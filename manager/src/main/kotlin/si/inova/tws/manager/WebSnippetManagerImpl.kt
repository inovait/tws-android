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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
import si.inova.tws.manager.data.SnippetStatus
import si.inova.tws.manager.data.SnippetType
import si.inova.tws.manager.data.WebSnippetDto
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.data.updateWith
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.local_handler.LocalSnippetHandler
import si.inova.tws.manager.local_handler.LocalSnippetHandlerImpl
import si.inova.tws.manager.network.WebSnippetFunction
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.service.NetworkConnectivityServiceImpl
import si.inova.tws.manager.web_socket.TwsSocket
import si.inova.tws.manager.web_socket.TwsSocketImpl
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class WebSnippetManagerImpl(
    context: Context,
    tag: String = DEFAULT_MANAGER_TAG,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val webSnippetFunction: WebSnippetFunction = BaseServiceFactory().create(),
    private val twsSocket: TwsSocket? = TwsSocketImpl(scope),
    private val networkConnectivityService: NetworkConnectivityService = NetworkConnectivityServiceImpl(context),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(scope),
    private val cacheManager: CacheManager? = FileCacheManager(context, tag)
) : WebSnippetManager {
    private val snippetsFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> = MutableStateFlow(Outcome.Progress())
    private val seenPopupSnippetsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    private var collectingSocket: Boolean = false
    private var collectingLocalHandler: Boolean = false

    private var orgId: String? = null
    private var projId: String? = null

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow

    // collect with collectAsStateWithLifecycle so the websocket reconnection will work
    override val popupSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter {
                it.type == SnippetType.POPUP && it.status == SnippetStatus.ENABLED
            }
        }
    }.distinctUntilChanged()

    // collect with collectAsStateWithLifecycle so the websocket reconnection will work
    override val contentSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter {
                it.type == SnippetType.TAB && it.status == SnippetStatus.ENABLED
            }
        }
    }.distinctUntilChanged()

    override val unseenPopupSnippetsFlow: Flow<List<WebSnippetDto>> = combine(
        popupSnippetsFlow,
        seenPopupSnippetsFlow
    ) { allPopups, seenPopups ->
        if (allPopups !is Outcome.Success) {
            emptyList()
        } else {
            allPopups.data.filter { !seenPopups.contains(it.id) }
        }
    }.distinctUntilChanged()

    init {
        scope.launch {
            networkConnectivityService.networkStatus.collect {
                when (it) {
                    is NetworkStatus.Connected -> {
                        twsSocket?.reconnect()
                    }

                    is NetworkStatus.Disconnected -> {
                        closeWebsocketConnection()
                    }
                }
            }
        }

        scope.launch {
            twsSocket?.socketStatus?.collect { status ->
                if (status is WebSocketStatus.Failed && status.response?.code == 401) {
                    val organizationId = orgId
                    val projectId = projId

                    if (organizationId != null && projectId != null) {
                        loadProjectAndSetupWss(organizationId, projectId)
                    }
                }
            }
        }
    }

    override suspend fun loadSharedSnippetData(shareId: String) {
        snippetsFlow.emit(Outcome.Progress(snippetsFlow.value.data))

        try {
            val sharedSnippet = webSnippetFunction.getSharedSnippetData(shareId).snippet
            _mainSnippetIdFlow.emit(sharedSnippet.id)
            loadProjectAndSetupWss(sharedSnippet.organizationId, sharedSnippet.projectId)
        } catch (e: CauseException) {
            snippetsFlow.emit(Outcome.Error(e, snippetsFlow.value.data))
        } catch (e: Exception) {
            snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), snippetsFlow.value.data))
        }
    }

    override suspend fun loadWebSnippets(organizationId: String, projectId: String) {
        try {
            orgId = organizationId
            projId = projectId
            loadProjectAndSetupWss(organizationId, projectId)
        } catch (e: CauseException) {
            snippetsFlow.emit(Outcome.Error(e, snippetsFlow.value.data))
        } catch (e: Exception) {
            snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), snippetsFlow.value.data))
        }
    }

    override fun closeWebsocketConnection() {
        twsSocket?.closeWebsocketConnection()
    }

    override fun markPopupsAsSeen(ids: List<String>) {
        seenPopupSnippetsFlow.value += ids
    }

    override fun release() {
        closeWebsocketConnection()
        localSnippetHandler?.release()
        scope.cancel()
    }

    private suspend fun loadProjectAndSetupWss(
        organizationId: String,
        projectId: String
    ) {
        snippetsFlow.emit(Outcome.Progress(cacheManager?.load(CACHED_SNIPPETS)))

        val twsProjectResponse = webSnippetFunction.getWebSnippets(organizationId, projectId, "someApiKey")
        val twsProject = twsProjectResponse.bodyOrThrow()

        localSnippetHandler?.calculateDateOffsetAndRerun(
            twsProjectResponse.headers().getDate(HEADER_DATE)?.toInstant(),
            twsProject.snippets
        )

        val wssUrl = twsProject.listenOn

        snippetsFlow.emit(Outcome.Success(twsProject.snippets))
        saveToCache(twsProject.snippets)

        twsSocket?.launchAndCollect(wssUrl)
        localSnippetHandler?.launchAndCollect(twsProject.snippets)
    }

    private fun saveToCache(snippets: List<WebSnippetDto>) = scope.launch(Dispatchers.IO) {
        try {
            cacheManager?.save(CACHED_SNIPPETS, snippets)
        } catch (e: Exception) {
            Log.e(TAG_ERROR_SAVE_CACHE, e.message, e)
        }
    }

    private fun TwsSocket.launchAndCollect(wssUrl: String) {
        setupWebSocketConnection(wssUrl)

        if (collectingSocket) return
        collectingSocket = true

        updateActionFlow.onEach {
            val oldList = snippetsFlow.value.data ?: emptyList()
            localSnippetHandler?.launchAndCollect(oldList)
            snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
            saveToCache(oldList.updateWith(it))
        }.launchIn(scope).invokeOnCompletion {
            collectingSocket = false
        }

        // Close web socket connection when there are no subscribers and reconnect when resubscribed
        scope.launch {
            SharingStarted.WhileSubscribed(5.seconds).command(snippetsFlow.subscriptionCount).collect {
                when (it) {
                    SharingCommand.START -> {
                        if (twsSocket?.connectionExists() == false) {
                            twsSocket.launchAndCollect(wssUrl)
                        }
                    }

                    SharingCommand.STOP,
                    SharingCommand.STOP_AND_RESET_REPLAY_CACHE -> {
                        closeWebsocketConnection()
                    }
                }
            }
        }
    }

    private suspend fun LocalSnippetHandler.launchAndCollect(snippets: List<WebSnippetDto>) {
        updateAndScheduleCheck(snippets)

        if (collectingLocalHandler) return
        collectingLocalHandler = true

        updateActionFlow.onEach {
            val oldList = snippetsFlow.value.data ?: emptyList()
            snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
            saveToCache(oldList.updateWith(it))
        }.launchIn(scope).invokeOnCompletion {
            collectingLocalHandler = false
        }
    }

    companion object {
        private const val DEFAULT_MANAGER_TAG = "ManagerSharedInstance"
        internal const val CACHED_SNIPPETS = "CachedSnippets"
        internal const val TAG_ERROR_SAVE_CACHE = "SaveCache"
        private const val HEADER_DATE = "date"

        private val instances = ConcurrentHashMap<String, WebSnippetManager>()

        fun getSharedInstance(
            context: Context,
            tag: String? = null,
        ): WebSnippetManager {
            val managerTag = tag ?: DEFAULT_MANAGER_TAG
            return instances.computeIfAbsent(managerTag) {
                WebSnippetManagerImpl(context, tag = managerTag)
            }
        }

        fun removeSharedInstance(tag: String?) {
            instances.remove(tag ?: DEFAULT_MANAGER_TAG)?.release()
        }
    }
}

