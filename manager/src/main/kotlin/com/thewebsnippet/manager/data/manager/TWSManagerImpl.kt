/*
 * Copyright 2025 INOVA IT d.o.o.
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
package com.thewebsnippet.manager.data.manager

import android.content.Context
import android.content.Intent
import com.thewebsnippet.manager.domain.datasource.CacheManager
import com.thewebsnippet.manager.data.datasource.FileCacheManager
import com.thewebsnippet.manager.core.TWSConfiguration
import com.thewebsnippet.manager.core.TWSManager
import com.thewebsnippet.manager.core.TWSOutcome
import com.thewebsnippet.manager.core.mapData
import com.thewebsnippet.manager.domain.model.NetworkStatus
import com.thewebsnippet.manager.domain.model.TWSSnippetDto
import com.thewebsnippet.manager.domain.model.toTWSSnippetList
import com.thewebsnippet.manager.domain.model.updateWith
import com.thewebsnippet.manager.domain.datasource.LocalSnippetHandler
import com.thewebsnippet.manager.data.datasource.LocalSnippetHandlerImpl
import com.thewebsnippet.manager.domain.datasource.SnippetLoadingManager
import com.thewebsnippet.manager.data.datasource.SnippetLoadingManagerImpl
import com.thewebsnippet.manager.domain.connectivity.NetworkConnectivityService
import com.thewebsnippet.manager.data.connectivity.NetworkConnectivityServiceImpl
import com.thewebsnippet.manager.data.datasource.RemoteCampaignLoaderImpl
import com.thewebsnippet.manager.domain.websocket.TWSSocket
import com.thewebsnippet.manager.data.websocket.TWSSocketImpl
import com.thewebsnippet.manager.domain.datasource.RemoteCampaignLoader
import com.thewebsnippet.manager.ui.TWSViewPopupActivity
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
 * An implementation of [TWSManager] responsible for managing snippets, including their loading,
 * caching, and live updates. It integrates WebSocket connections, local snippet handlers, and
 * network connectivity monitoring to ensure that snippets remain up-to-date and responsive to changes.
 *
 * @param context The application context, used for cache management and connectivity services.
 * @param tag A unique identifier used for cache namespacing and internal logging.
 * @param configuration Configuration settings defining the source and behavior of snippet management.
 * @param scope A [CoroutineScope] used for running asynchronous tasks and managing their lifecycle.
 * @param remoteSnippetLoader A [SnippetLoadingManager] that fetches snippets from a remote source.
 * @param cacheSnippetLoader An optional cache manager that retrieves and stores snippets locally.
 * @param remoteSnippetUpdater An optional WebSocket-based updater for receiving live snippet updates.
 * @param localSnippetUpdater An optional handler for local snippet-related updates (e.g., visibility changes).
 * @param remoteCampaignLoader An optional loader for retrieving remote campaign-related snippets.
 * @param networkConnectivityService An optional service for observing network connectivity status.
 */
internal class TWSManagerImpl(
    private val context: Context,
    tag: String = "",
    private val configuration: TWSConfiguration,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val remoteSnippetLoader: SnippetLoadingManager = SnippetLoadingManagerImpl(context, configuration),
    private val cacheSnippetLoader: CacheManager? = FileCacheManager(context, tag),
    private val remoteSnippetUpdater: TWSSocket? = TWSSocketImpl(scope),
    private val localSnippetUpdater: LocalSnippetHandler? = LocalSnippetHandlerImpl(scope),
    private val remoteCampaignLoader: RemoteCampaignLoader? = RemoteCampaignLoaderImpl(context, configuration),
    private val networkConnectivityService: NetworkConnectivityService? = NetworkConnectivityServiceImpl(context)
) : TWSManager, CoroutineScope by scope {

    private var isRegistered: Boolean = false
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
    }.onStart { loadProjectAndSnippets() }
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
     * Creates a connection to a backend service and prepares snippets, note that this MUST be called before manager can
     * actually be used, otherwise connection to a backend service will not be established.
     */
    override fun register() {
        if (isRegistered) return

        isRegistered = true
        loadProjectAndSnippets()
    }

    /**
     * Forces a refresh of the snippets by reloading them from the remote source.
     * Updates are emitted to all active collectors and cached for future use.
     */
    override fun forceRefresh() {
        launch {
            try {
                _snippetsFlow.emit(TWSOutcome.Progress(cacheSnippetLoader?.load(CACHED_SNIPPETS)))

                val response = remoteSnippetLoader.load()
                val project = response.project

                _snippetsFlow.emit(TWSOutcome.Success(project.snippets))

                saveToCache(project.snippets)

                remoteSnippetUpdater?.launchAndCollect(project.listenOn)
                localSnippetUpdater?.launchAndCollect(response.responseDate, project.snippets)
            } catch (e: Exception) {
                _snippetsFlow.emit(TWSOutcome.Error(e, _snippetsFlow.value?.data))
            }
        }
    }

    /**
     * Logs a user-defined event and fetches campaign-based snippets from the backend.
     * If the backend returns any matching campaign snippets, all are displayed immediately
     * using a popup UI ([TWSViewPopupActivity]).
     *
     *
     * @param event The name of the event to log and use for campaign targeting.
     */
    override fun logEvent(event: String) {
        launch {
            val snippetsToDisplay = remoteCampaignLoader?.logEventAndGetCampaignSnippets(event).orEmpty()
            snippetsToDisplay.forEach {
                val intent = TWSViewPopupActivity.createIntent(context, it.id, it.projectId).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
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
        cacheSnippetLoader?.save(CACHED_SNIPPETS, snippets)
    }

    private fun loadProjectAndSnippets() {
        if (!isRegistered) return

        // web socket and local handler will be launched in here
        forceRefresh()

        // start collecting network status, will enable us to disconnect/reconnect
        networkConnectivityService?.launchAndCollect()
    }

    private fun cancelCollecting() {
        localSnippetUpdater?.release()
        remoteSnippetUpdater?.closeWebsocketConnection()

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

            localSnippetUpdater?.updateAndScheduleCheck(newList)
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
                    is NetworkStatus.Disconnected -> remoteSnippetUpdater?.closeWebsocketConnection()
                }
            }
        }
    }

    companion object {
        internal const val CACHED_SNIPPETS = "CachedSnippets"
    }
}
