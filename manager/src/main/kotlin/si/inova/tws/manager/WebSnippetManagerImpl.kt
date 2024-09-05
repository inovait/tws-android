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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.downgradeTo
import si.inova.kotlinova.core.outcome.mapData
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.SnippetStatus
import si.inova.tws.manager.data.SnippetType
import si.inova.tws.manager.data.WebSnippetDto
import si.inova.tws.manager.factory.BaseServiceFactory
import si.inova.tws.manager.factory.create
import si.inova.tws.manager.local_handler.LocalSnippetHandler
import si.inova.tws.manager.local_handler.LocalSnippetHandlerImpl
import si.inova.tws.manager.network.WebSnippetFunction
import si.inova.tws.manager.singleton.coroutineResourceManager
import si.inova.tws.manager.web_socket.TwsSocket
import si.inova.tws.manager.web_socket.TwsSocketImpl

class WebSnippetManagerImpl(
    context: Context,
    private val resources: CoroutineResourceManager = coroutineResourceManager,
    private val webSnippetFunction: WebSnippetFunction = BaseServiceFactory().create(),
    private val twsSocket: TwsSocket? = TwsSocketImpl(context, resources.scope),
    private val localSnippetHandler: LocalSnippetHandler? = LocalSnippetHandlerImpl(resources.scope)
) : WebSnippetManager {
    private val snippetsFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> = MutableStateFlow(Outcome.Progress())

    private val _mainSnippetIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val mainSnippetIdFlow: Flow<String?> = _mainSnippetIdFlow

    override val popupSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter { it.type == SnippetType.POPUP && it.status == SnippetStatus.ENABLED }
        }
    }

    override val contentSnippetsFlow = snippetsFlow.map { outcome ->
        outcome.mapData { data ->
            data.filter { it.type == SnippetType.TAB && it.status == SnippetStatus.ENABLED }
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
            snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), _snippetsFlow.value.data))
        }
    }

    override suspend fun loadWebSnippets(organizationId: String, projectId: String) {
        try {
            loadProjectAndSetupWss(organizationId, projectId)
        } catch (e: CauseException) {
            snippetsFlow.emit(Outcome.Error(e, snippetsFlow.value.data))
        } catch (e: Exception) {
            snippetsFlow.emit(Outcome.Error(UnknownCauseException("", e), _snippetsFlow.value.data))
        }
    }

    override fun closeWebsocketConnection() {
        twsSocket?.closeWebsocketConnection()
    }

    private suspend fun loadProjectAndSetupWss(
        organizationId: String,
        projectId: String
    ) {
        val twsProject = webSnippetFunction.getWebSnippets(organizationId, projectId, "someApiKey")
        val wssUrl = twsProject.listenOn

        snippetsFlow.emit(Outcome.Success(twsProject.snippets))

        twsSocket?.launchAndCollect(wssUrl)
        localSnippetHandler?.launchAndCollect(twsProject.snippets)
    }

    private fun TwsSocket.launchAndCollect(wssUrl: String) {
        setupWebSocketConnection(wssUrl)
        updateActionFlow.onEach {
            val oldList = snippetsFlow.value.data ?: emptyList()
            snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
        }.launchIn(resources.scope)
    }

    private suspend fun LocalSnippetHandler.launchAndCollect(snippets: List<WebSnippetDto>) {
        updateAndScheduleCheck(snippets)
        updateActionFlow.onEach {
            val oldList = _snippetsFlow.value.data ?: emptyList()
            _snippetsFlow.emit(Outcome.Success(oldList.updateWith(it)))
        }.launchIn(resources.scope)
    }

    private fun List<WebSnippetDto>.updateWith(action: SnippetUpdateAction): List<WebSnippetDto> {
        return when (action.type) {
            ActionType.CREATED -> insert(action.data)
            ActionType.UPDATED -> update(action.data)
            ActionType.DELETED -> remove(action.data)
        }
    }

    private fun List<WebSnippetDto>.insert(data: ActionBody): List<WebSnippetDto> {
        return toMutableList().apply {
            if (data.target != null && data.organizationId != null && data.projectId != null) {
                add(
                    WebSnippetDto(
                        id = data.id,
                        target = data.target,
                        headers = data.headers ?: emptyMap(),
                        organizationId = data.organizationId,
                        projectId = data.projectId
                    )
                )
            }
        }
    }

    private fun List<WebSnippetDto>.update(data: ActionBody): List<WebSnippetDto> {
        return map {
            if (it.id == data.id) {
                it.copy(
                    loadIteration = it.loadIteration + 1,
                    target = data.target ?: it.target,
                    headers = data.headers ?: it.headers,
                    html = data.html ?: it.html
                )
            } else {
                it
            }
        }
    }

    private fun List<WebSnippetDto>.remove(data: ActionBody): List<WebSnippetDto> {
        return filter { it.id != data.id }
    }
}
