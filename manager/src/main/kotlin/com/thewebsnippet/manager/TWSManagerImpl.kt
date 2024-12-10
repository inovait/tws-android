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

package com.thewebsnippet.manager

import android.content.Context
import com.thewebsnippet.manager.cache.CacheManager
import com.thewebsnippet.manager.cache.FileCacheManager
import com.thewebsnippet.manager.data.NetworkStatus
import com.thewebsnippet.manager.data.TWSSnippetDto
import com.thewebsnippet.manager.data.toTWSSnippetList
import com.thewebsnippet.manager.data.updateWith
import com.thewebsnippet.manager.localhandler.LocalSnippetHandler
import com.thewebsnippet.manager.localhandler.LocalSnippetHandlerImpl
import com.thewebsnippet.manager.manager.snippet.SnippetLoadingManager
import com.thewebsnippet.manager.manager.snippet.SnippetLoadingManagerImpl
import com.thewebsnippet.manager.service.NetworkConnectivityService
import com.thewebsnippet.manager.service.NetworkConnectivityServiceImpl
import com.thewebsnippet.manager.websocket.TWSSocket
import com.thewebsnippet.manager.websocket.TWSSocketImpl
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
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * An implementation of [TWSManager] that manages snippets, their loading, caching, and live updates.
 * It integrates WebSocket connections, local snippet handler, and network connectivity to ensure
 * snippets are always up-to-date and react to changes dynamically.
 *
 * @param context The application context, used for cache management and connectivity services.
 * @param tag A unique tag used for cache management and identification.
 * @param configuration The configuration for the manager, defining source of the snippets.
 * @param scope A [CoroutineScope] used for asynchronous operations and lifecycle management.
 * @param loader A [SnippetLoadingManager] responsible for loading snippets from a remote source.
 * @param twsSocket An optional WebSocket connection to receive live updates.
 * @param localSnippetHandler An optional handler for local snippet updates, like visibility changes.
 * @param cacheManager An optional cache manager to handle snippet caching.
 * @param networkConnectivityService An optional service to monitor network connectivity changes.
 */
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

    private val _snippetsFlow: MutableStateFlow<TWSOutcome<List<TWSSnippetDto>>?> = MutableStateFlow(null)
    private val _localProps: MutableStateFlow<Map<String, Map<String, Any>>> = MutableStateFlow(emptyMap())

    /**
     * A flow that combines remote snippets with local properties to keep the data in sync and up-to-date.
     * Use `collectAsStateWithLifecycle` to automatically start and stop data collection based on the lifecycle.
     */
    override val snippets = combine(_snippetsFlow.filterNotNull(), _localProps) { outcome, localProps ->
        outcome.mapData { it.toTWSSnippetList(localProps) }
    }.onStart { setupCollectingAndLoad() }
        .onCompletion { cancelCollecting() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5.seconds), null)
        .filterNotNull()

    /**
     * Retrieves the list of snippets as a flow of data only.
     *
     * @return A [Flow] emitting the current list of snippets or `null` if unavailable.
     */
    override fun snippets() = snippets.map { it.data }

    /**
     * Forces a refresh of the snippets by reloading them from the remote source.
     * Updates are emitted to all active collectors and cached for future use.
     */
    override fun forceRefresh() {
        launch {
            try {
                _snippetsFlow.emit(TWSOutcome.Progress(cacheManager?.load(CACHED_SNIPPETS)))

                val response = loader.load()
                val project = response.project

                _snippetsFlow.emit(TWSOutcome.Success(project.snippets))

                saveToCache(project.snippets)

                twsSocket?.launchAndCollect(project.listenOn)
                localSnippetHandler?.launchAndCollect(response.responseDate, project.snippets)
            } catch (e: Exception) {
                _snippetsFlow.emit(TWSOutcome.Error(e, _snippetsFlow.value?.data))
            }
        }
    }

    /**
     * Updates or adds local properties for a specific snippet.
     * These properties are  applied to the snippet for all active collectors.
     *
     * @param id The unique identifier of the snippet.
     * @param localProps A map of properties to associate with the snippet.
     */
    override fun set(id: String, localProps: Map<String, Any>) {
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
            val newList = _snippetsFlow.value?.data.orEmpty().updateWith(it)

            localSnippetHandler?.updateAndScheduleCheck(newList)
            _snippetsFlow.emit(TWSOutcome.Success(newList))
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
            val newList = _snippetsFlow.value?.data.orEmpty().updateWith(it)

            _snippetsFlow.emit(TWSOutcome.Success(newList))
            saveToCache(newList)
        }.launchIn(this@TWSManagerImpl).invokeOnCompletion {
            collectingLocalHandler = false
        }
    }

    // Ensure snippets are refreshed and socket reestablished after connection is reestablished
    private fun NetworkConnectivityService.launchAndCollect() {
        if (networkStatusJob?.isActive == true) return

        var ignoreFirst = networkConnectivityService?.isConnected == true
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
