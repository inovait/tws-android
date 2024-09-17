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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.retrofit.callfactory.bodyOrThrow
import si.inova.tws.manager.cache.CacheManager
import si.inova.tws.manager.cache.FileCacheManager
import si.inova.tws.manager.data.SnippetStatus
import si.inova.tws.manager.data.SnippetType
import si.inova.tws.manager.data.WebSnippetDto
import si.inova.tws.manager.data.updateWith
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.local_handler.LocalSnippetHandler
import si.inova.tws.manager.local_handler.LocalSnippetHandlerImpl
import si.inova.tws.manager.network.WebSnippetFunction
import si.inova.tws.manager.singleton.coroutineResourceManager
import si.inova.tws.manager.web_socket.TwsSocket
import si.inova.tws.manager.web_socket.TwsSocketImpl
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class WebSnippetManagerImpl(
    context: Context,
    private val resources: CoroutineResourceManager = coroutineResourceManager,
    private val webSnippetFunction: WebSnippetFunction = BaseServiceFactory().create(),
    private val twsSocket: TwsSocket? = TwsSocketImpl(context, resources.scope),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(resources.scope),
    private val cacheManager: CacheManager? = FileCacheManager(context)
) : WebSnippetManager {
    private val snippetsFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> = MutableStateFlow(Outcome.Progress())

    private var collectingSocket: Boolean = false
    private var collectingLocalHandler: Boolean = false

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow

    override val popupSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter {
                it.type == SnippetType.POPUP && it.status == SnippetStatus.ENABLED
            }
        }
    }

    override val contentSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter {
                it.type == SnippetType.TAB && it.status == SnippetStatus.ENABLED
            }
        }
    }

    init {
        resources.scope.launch {
            snippetsFlow.collect { outcome ->
                outcome.data?.let {
                    localSnippetHandler?.launchAndCollect(it)
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
    }

    private fun saveToCache(snippets: List<WebSnippetDto>) = resources.scope.launch(Dispatchers.IO) {
        try {
            cacheManager?.save(CACHED_SNIPPETS, snippets)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun TwsSocket.launchAndCollect(wssUrl: String) {
        if (collectingSocket) return
        collectingSocket = true

        setupWebSocketConnection(wssUrl)
        updateActionFlow.onEach {
            val oldList = snippetsFlow.value.data ?: emptyList()
            snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
        }.launchIn(resources.scope).invokeOnCompletion {
            collectingSocket = false
        }
    }

    private suspend fun LocalSnippetHandler.launchAndCollect(snippets: List<WebSnippetDto>) {
        updateAndScheduleCheck(snippets)

        if (collectingLocalHandler) return
        collectingLocalHandler = true

        updateActionFlow.onEach {
            val oldList = snippetsFlow.value.data ?: emptyList()
            snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
        }.launchIn(resources.scope).invokeOnCompletion {
            collectingLocalHandler = false
        }
    }

    companion object {
        private const val DEFAULT_MANAGER_TAG = "ManagerSharedInstance"
        internal const val CACHED_SNIPPETS = "CachedSnippets"
        private const val HEADER_DATE: String = "date"
        private const val HEADER_DATE_PATTERN: String = "EEE, dd MMM yyyy HH:mm:ss z"

        private val instances = ConcurrentHashMap<String, WebSnippetManager>()

        fun getSharedInstance(
            context: Context,
            tag: String? = null,
        ): WebSnippetManager {
            return instances.computeIfAbsent(tag ?: DEFAULT_MANAGER_TAG) {
                WebSnippetManagerImpl(context)
            }
        }

        fun removeSharedInstance(key: String) {
            instances.remove(key)
        }
    }
}

