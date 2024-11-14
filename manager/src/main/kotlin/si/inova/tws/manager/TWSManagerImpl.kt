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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.tws.manager.cache.CacheManager
import si.inova.tws.manager.cache.FileCacheManager
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.TWSSnippetDto
import si.inova.tws.manager.data.toTWSSnippetList
import si.inova.tws.manager.data.updateWith
import si.inova.tws.manager.localhandler.LocalSnippetHandler
import si.inova.tws.manager.localhandler.LocalSnippetHandlerImpl
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.service.NetworkConnectivityServiceImpl
import si.inova.tws.manager.snippet.SnippetLoadingManager
import si.inova.tws.manager.snippet.SnippetLoadingManagerImpl
import si.inova.tws.manager.websocket.TWSSocket
import si.inova.tws.manager.websocket.TWSSocketImpl
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class TWSManagerImpl(
    context: Context,
    tag: String = "",
    private val configuration: TWSConfiguration,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val loader: SnippetLoadingManager = SnippetLoadingManagerImpl(configuration),
    private val twsSocket: TWSSocket? = TWSSocketImpl(scope),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(scope),
    private val cacheManager: CacheManager? = FileCacheManager(context, tag),
    private val networkConnectivityService: NetworkConnectivityService? = NetworkConnectivityServiceImpl(context)
) : TWSManager, CoroutineScope by scope {

    private var collectingSocket: Boolean = false
    private var collectingLocalHandler: Boolean = false
    private var networkStatusJob: Job? = null

    private val _snippetsFlow: MutableStateFlow<Outcome<List<TWSSnippetDto>>> = MutableStateFlow(Outcome.Progress())
    private val _localProps: MutableStateFlow<Map<String, Map<String, Any>>> = MutableStateFlow(emptyMap())

    /**
     * collect [snippets] using `collectAsStateWithLifecycle`
     * to ensure the WebSocket disconnects when not needed and reconnects appropriately.
     */
    override val snippets = combine(_snippetsFlow, _localProps) { outcome, localProps ->
        outcome.mapData { it.toTWSSnippetList(localProps) }
    }.onStart { setupCollectingAndLoad() }
        .onCompletion { cancelCollecting() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5.seconds), Outcome.Progress())

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow.filterNotNull()

    override fun snippets() = snippets.map { it.data }

    override fun forceRefresh() {
        launch {
            try {
                _snippetsFlow.emit(Outcome.Progress(cacheManager?.load(CACHED_SNIPPETS)))

                val response = loader.load()
                val project = response.project

                _mainSnippetIdFlow.emit(response.mainSnippet)

                _snippetsFlow.emit(Outcome.Success(project.snippets))

                saveToCache(project.snippets)

                twsSocket?.launchAndCollect(project.listenOn)
                localSnippetHandler?.launchAndCollect(response.responseDate, project.snippets)
            } catch (e: CauseException) {
                _snippetsFlow.emit(Outcome.Error(e, _snippetsFlow.value.data))
            } catch (e: Exception) {
                _snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), _snippetsFlow.value.data))
            }
        }
    }

    override fun setLocalProps(id: String, localProps: Map<String, Any>) {
        val currentLocalProps = _localProps.value.toMutableMap()
        currentLocalProps[id] = localProps
        _localProps.update { currentLocalProps }
    }

    private fun saveToCache(snippets: List<TWSSnippetDto>) = launch {
        cacheManager?.save(CACHED_SNIPPETS, snippets)
    }

    private fun setupCollectingAndLoad() {
        // web socket and local handler will be launched in here
        forceRefresh()

        // start collecting network status, will enable us to disconnect/reconnect
        networkConnectivityService?.launchAndCollect()
    }

    private fun cancelCollecting() {
        localSnippetHandler?.release()
        twsSocket?.closeWebsocketConnection()

        // stop collecting network status
        networkStatusJob?.cancel()
        networkStatusJob = null
    }

    // Process remote snippet changes (from web socket)
    private fun TWSSocket.launchAndCollect(wssUrl: String) {
        setupWebSocketConnection(wssUrl, ::forceRefresh)

        if (collectingSocket) return
        collectingSocket = true

        updateActionFlow.onEach {
            val newList = _snippetsFlow.value.data.orEmpty().updateWith(it)

            localSnippetHandler?.updateAndScheduleCheck(newList)
            _snippetsFlow.emit(Outcome.Success(newList))
            saveToCache(newList)
        }.launchIn(this@TWSManagerImpl).invokeOnCompletion {
            collectingSocket = false
        }
    }

    // Process local snippet changes (hiding with visibility)
    private suspend fun LocalSnippetHandler.launchAndCollect(serverDate: Instant, snippets: List<TWSSnippetDto>) {
        calculateDateOffsetAndRerun(serverDate, snippets)

        if (collectingLocalHandler) return
        collectingLocalHandler = true

        updateActionFlow.onEach {
            val newList = _snippetsFlow.value.data.orEmpty().updateWith(it)

            _snippetsFlow.emit(Outcome.Success(newList))
            saveToCache(newList)
        }.launchIn(this@TWSManagerImpl).invokeOnCompletion {
            collectingLocalHandler = false
        }
    }

    // Ensure snippets are refreshed and socket reestablished after connection is reestablished
    private fun NetworkConnectivityService.launchAndCollect() {
        if (networkStatusJob?.isActive == true) return

        var ignoreFirst = true
        networkStatusJob = launch {
            networkStatus.collect {
                if (ignoreFirst) {
                    ignoreFirst = false
                    return@collect
                }

                when (it) {
                    is NetworkStatus.Connected -> forceRefresh()
                    is NetworkStatus.Disconnected -> twsSocket?.closeWebsocketConnection()
                }
            }
        }
    }

    companion object {
        internal const val CACHED_SNIPPETS = "CachedSnippets"
    }
}
