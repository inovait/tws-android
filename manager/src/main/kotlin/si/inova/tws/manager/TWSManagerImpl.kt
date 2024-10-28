/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.manager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.retrofit.callfactory.bodyOrThrow
import si.inova.tws.data.WebSnippetDto
import si.inova.tws.manager.cache.CacheManager
import si.inova.tws.manager.cache.FileCacheManager
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.data.updateWith
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.localhandler.LocalSnippetHandler
import si.inova.tws.manager.localhandler.LocalSnippetHandlerImpl
import si.inova.tws.manager.network.WebSnippetFunction
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.service.NetworkConnectivityServiceImpl
import si.inova.tws.manager.websocket.TwsSocket
import si.inova.tws.manager.websocket.TwsSocketImpl
import si.inova.tws.manager.websocket.TwsSocketImpl.Companion.ERROR_UNAUTHORIZED
import kotlin.time.Duration.Companion.seconds

class TWSManagerImpl(
    context: Context,
    private val configuration: TWSConfiguration,
    tag: String = "",
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val webSnippetFunction: WebSnippetFunction = BaseServiceFactory().create(),
    private val twsSocket: TwsSocket? = TwsSocketImpl(scope),
    private val networkConnectivityService: NetworkConnectivityService = NetworkConnectivityServiceImpl(context),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(scope),
    private val cacheManager: CacheManager? = FileCacheManager(context, tag),
) : TWSManager, CoroutineScope by scope {
    private val _snippetsFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> = MutableStateFlow(Outcome.Progress())
    private val _localProps: MutableStateFlow<Map<String, Map<String, Any>>> = MutableStateFlow(emptyMap())

    // collect with collectAsStateWithLifecycle so the websocket reconnection will work
    override val snippetsFlow = combine(_snippetsFlow, _localProps) { snippetsOutcome, localProps ->
        snippetsOutcome.mapData { snippets ->
            snippets.map {
                it.copy(props = it.props + (localProps[it.id] ?: emptyMap()))
            }
        }
    }

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow

    private var collectingSocket: Boolean = false
    private var collectingLocalHandler: Boolean = false

    private var orgId: String? = null
    private var projId: String? = null

    init {
        observeNetworkConnectivity()
        observeWebSocketStatus()
    }

    override fun run() {
        launch {
            if (configuration is TWSConfiguration.Basic) {
                loadProjectAndSetupWss(organizationId = configuration.organizationId, projectId = configuration.projectId)
            } else if (configuration is TWSConfiguration.Shared) {
                loadSharedSnippetData(sharedId = configuration.sharedId)
            }
        }
    }

    override fun setLocalProps(id: String, localProps: Map<String, Any>) {
        launch {
            val currentLocalProps = _localProps.value.toMutableMap()
            currentLocalProps[id] = (currentLocalProps[id] ?: emptyMap()) + localProps
            _localProps.emit(currentLocalProps)
        }
    }

    override fun closeWebsocketConnection() {
        twsSocket?.closeWebsocketConnection()
    }

    private suspend fun loadSharedSnippetData(sharedId: String) {
        try {
            val sharedSnippet = webSnippetFunction.getSharedSnippetData(sharedId).snippet
            _mainSnippetIdFlow.emit(sharedSnippet.id)
            loadProjectAndSetupWss(sharedSnippet.organizationId, sharedSnippet.projectId)
        } catch (e: CauseException) {
            _snippetsFlow.emit(Outcome.Error(e, _snippetsFlow.value.data))
        } catch (e: Exception) {
            _snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), _snippetsFlow.value.data))
        }
    }

    private suspend fun loadProjectAndSetupWss(
        organizationId: String,
        projectId: String
    ) {
        orgId = organizationId
        projId = projectId

        try {
            _snippetsFlow.emit(Outcome.Progress(cacheManager?.load(CACHED_SNIPPETS)))

            val twsProjectResponse = webSnippetFunction.getWebSnippets(organizationId, projectId, "someApiKey")
            val twsProject = twsProjectResponse.bodyOrThrow()

            localSnippetHandler?.calculateDateOffsetAndRerun(
                twsProjectResponse.headers().getDate(HEADER_DATE)?.toInstant(),
                twsProject.snippets
            )

            val wssUrl = twsProject.listenOn

            _snippetsFlow.emit(Outcome.Success(twsProject.snippets))
            saveToCache(twsProject.snippets)

            twsSocket?.launchAndCollect(wssUrl)
            localSnippetHandler?.launchAndCollect(twsProject.snippets)
        } catch (e: CauseException) {
            _snippetsFlow.emit(Outcome.Error(e, _snippetsFlow.value.data))
        } catch (e: Exception) {
            _snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), _snippetsFlow.value.data))
        }
    }

    private fun saveToCache(snippets: List<WebSnippetDto>) = launch() {
        try {
            cacheManager?.save(CACHED_SNIPPETS, snippets)
        } catch (e: Exception) {
            Log.e(TAG_ERROR_SAVE_CACHE, e.message, e)
        }
    }

    // Collect remote snippet changes (from web socket)
    private fun TwsSocket.launchAndCollect(wssUrl: String) {
        setupWebSocketConnection(wssUrl)

        if (collectingSocket) return
        collectingSocket = true

        val organizationId = orgId ?: error("Organization id should be available")
        val projectId = projId ?: error("Project id should be available")

        updateActionFlow.onEach {
            val newList = _snippetsFlow.value.data.orEmpty().updateWith(it, organizationId, projectId)

            localSnippetHandler?.launchAndCollect(newList)
            _snippetsFlow.emit(Outcome.Success(newList))
            saveToCache(newList)
        }.launchIn(this@TWSManagerImpl).invokeOnCompletion {
            collectingSocket = false
        }

        setupSocketReconnectionLifecycle()
    }

    // Collect local snippet changes (hiding with visibility)
    private suspend fun LocalSnippetHandler.launchAndCollect(snippets: List<WebSnippetDto>) {
        updateAndScheduleCheck(snippets)

        if (collectingLocalHandler) return
        collectingLocalHandler = true

        val organizationId = orgId ?: error("Organization id should be available")
        val projectId = projId ?: error("Project id should be available")

        updateActionFlow.onEach {
            val newList = _snippetsFlow.value.data.orEmpty().updateWith(it, organizationId, projectId)

            _snippetsFlow.emit(Outcome.Success(newList))
            saveToCache(newList)
        }.launchIn(this@TWSManagerImpl).invokeOnCompletion {
            collectingLocalHandler = false
        }
    }

    // Close web socket connection when there are no subscribers and reload all data with new socket connection when resubscribed
    private fun setupSocketReconnectionLifecycle() = launch {
        SharingStarted.WhileSubscribed(5.seconds).command(_snippetsFlow.subscriptionCount).collect {
            when (it) {
                SharingCommand.START -> {
                    val organizationId = orgId
                    val projectId = projId

                    if (organizationId != null && projectId != null) {
                        loadProjectAndSetupWss(organizationId, projectId)
                    }
                }

                SharingCommand.STOP,
                SharingCommand.STOP_AND_RESET_REPLAY_CACHE -> {
                    closeWebsocketConnection()
                }
            }
        }
    }

    private fun observeNetworkConnectivity() = launch {
        networkConnectivityService.networkStatus.collect { status ->
            when (status) {
                is NetworkStatus.Connected -> {
                    twsSocket?.reconnect()
                }
                is NetworkStatus.Disconnected -> {
                    closeWebsocketConnection()
                }
            }
        }
    }

    private fun observeWebSocketStatus() = launch {
        twsSocket?.socketStatus?.collect { status ->
            if (status is WebSocketStatus.Failed && status.response?.code == ERROR_UNAUTHORIZED) {
                val organizationId = orgId
                val projectId = projId

                if (organizationId != null && projectId != null) {
                    loadProjectAndSetupWss(organizationId, projectId)
                }
            }
        }
    }

    companion object {
        internal const val CACHED_SNIPPETS = "CachedSnippets"

        private const val TAG_ERROR_SAVE_CACHE = "SaveCache"
        private const val HEADER_DATE = "date"
    }
}
