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
import si.inova.tws.manager.cache.CacheManager
import si.inova.tws.manager.cache.FileCacheManager
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.WebSnippetDto
import si.inova.tws.manager.data.toTWSSnippet
import si.inova.tws.manager.data.updateWith
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.localhandler.LocalSnippetHandler
import si.inova.tws.manager.localhandler.LocalSnippetHandlerImpl
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.service.NetworkConnectivityServiceImpl
import si.inova.tws.manager.websocket.TWSSocket
import si.inova.tws.manager.websocket.TWSSocketImpl
import kotlin.time.Duration.Companion.seconds

internal class TWSManagerImpl(
    context: Context,
    tag: String = "",
    private val configuration: TWSConfiguration,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val functions: TWSFunctions = BaseServiceFactory().create(),
    private val twsSocket: TWSSocket? = TWSSocketImpl(context, scope),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(scope),
    private val cacheManager: CacheManager? = FileCacheManager(context, tag),
    private val networkConnectivityService: NetworkConnectivityService? = NetworkConnectivityServiceImpl(context)
) : TWSManager, CoroutineScope by scope {

    private val _snippetsFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> = MutableStateFlow(Outcome.Progress())
    private val _localProps: MutableStateFlow<Map<String, Map<String, Any>>> = MutableStateFlow(emptyMap())

    /**
     * collect [snippetsFlow] using `collectAsStateWithLifecycle`
     * to ensure the WebSocket disconnects when not needed and reconnects appropriately.
     */
    override val snippetsFlow = combine(_snippetsFlow, _localProps) { snippetsOutcome, localProps ->
        snippetsOutcome.mapData { snippets ->
            snippets.map {
                it.toTWSSnippet(localProps[it.id].orEmpty())
            }
        }
    }.onStart {
        forceRefresh()
    }.onCompletion {
        twsSocket?.closeWebsocketConnection()
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5.seconds),
        Outcome.Progress()
    )

    init {
        setupNetworkConnectivityHandling()
    }

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow

    private var collectingSocket: Boolean = false
    private var collectingLocalHandler: Boolean = false

    private var orgId: String? = null
    private var projId: String? = null

    override fun run() {
        launch {
            _snippetsFlow.emit(Outcome.Progress(cacheManager?.load(CACHED_SNIPPETS)))
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
            currentLocalProps[id] = localProps
            _localProps.emit(currentLocalProps)
        }
    }

    private suspend fun loadSharedSnippetData(sharedId: String) {
        try {
            val sharedSnippet = functions.getSharedSnippetData(sharedId).snippet
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
            val twsProjectResponse = functions.getWebSnippets(organizationId, projectId, configuration.apiKey)
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

    private fun saveToCache(snippets: List<WebSnippetDto>) = launch {
        cacheManager?.save(CACHED_SNIPPETS, snippets)
    }

    // Collect remote snippet changes (from web socket)
    private fun TWSSocket.launchAndCollect(wssUrl: String) {
        setupWebSocketConnection(wssUrl, ::refreshProject)

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

    private fun setupNetworkConnectivityHandling() {
        if (networkConnectivityService == null) return

        launch {
            networkConnectivityService.networkStatus.collect {
                when (it) {
                    is NetworkStatus.Connected -> forceRefresh()
                    is NetworkStatus.Disconnected -> twsSocket?.closeWebsocketConnection()
                }
            }
        }
    }

    private fun refreshProject() = launch {
        val organizationId = orgId
        val projectId = projId

        if (organizationId != null && projectId != null) {
            loadProjectAndSetupWss(organizationId, projectId)
        }
    }

    companion object {
        internal const val CACHED_SNIPPETS = "CachedSnippets"
        private const val HEADER_DATE = "date"
    }
}
