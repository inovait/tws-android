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

package si.inova.tws.repo

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.downgradeTo
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.repo.data.NetworkStatus
import si.inova.tws.repo.data.WebSnippetDto
import si.inova.tws.repo.factory.BaseServiceFactory
import si.inova.tws.repo.factory.create
import si.inova.tws.repo.network.WebSnippetFunction
import si.inova.tws.repo.service.NetworkConnectivityService
import si.inova.tws.repo.service.NetworkConnectivityServiceImpl
import si.inova.tws.repo.singleton.coroutineResourceManager
import si.inova.tws.web_socket.TwsSocket

class WebSnippetManagerImpl(context: Context) : WebSnippetManager {
   private val resources: CoroutineResourceManager = coroutineResourceManager
   private val webSnippetFunction: WebSnippetFunction = BaseServiceFactory().create()
   private val networkConnectivityService: NetworkConnectivityService = NetworkConnectivityServiceImpl(context)

   private val twsSocket: TwsSocket = TwsSocket(resources.scope)

   // needs storing to allow us to reconnect if connection fails because of the network issues
   private var wssUrl: String? = null

   private val stateFlow: MutableStateFlow<Outcome<List<WebSnippetDto>>> =
      MutableStateFlow(Outcome.Progress())

   override val snippetsFlow = combine(
      twsSocket.snippetsFlow,
      stateFlow
   ) { snippets, state ->
      Outcome.Success(snippets).downgradeTo(state)
   }

   init {
      resources.scope.launch {
         networkConnectivityService.networkStatus.collect {
            if (twsSocket.socketExists() && it is NetworkStatus.Connected) {
               setupWebSocketConnection()
            }
         }
      }
   }

   override suspend fun loadWebSnippets(organizationsId: String, projectId: String) {
      try {
         loadProjectAndSetupWss(organizationsId, projectId)
      } catch (e: CauseException) {
         stateFlow.emit(Outcome.Error(e, stateFlow.value.data))
      } catch (e: Exception) {
         stateFlow.emit(Outcome.Error(UnknownCauseException("", e), stateFlow.value.data))
      }
   }

   private suspend fun loadProjectAndSetupWss(
      organizationId: String,
      projectId: String
   ) {
      val twsProject = webSnippetFunction.getWebSnippets(organizationId, projectId, "someApiKey")

      wssUrl = twsProject.listenOn
      setupWebSocketConnection()

      val webSnippets = twsProject.snippets.map {
         WebSnippetData(it.id, it.target, it.headers ?: emptyMap(), it.loadIteration)
      }

      twsSocket.manuallyUpdateSnippet(webSnippets)
   }

   private fun setupWebSocketConnection() {
      val wss = wssUrl?.takeIf { it.isNotEmpty() } ?: return

      resources.scope.launch {
         twsSocket.setupWebSocketConnection(wss)
      }
   }

   override fun closeWebsocketConnection() {
      twsSocket.closeWebsocketConnection()
   }
}
