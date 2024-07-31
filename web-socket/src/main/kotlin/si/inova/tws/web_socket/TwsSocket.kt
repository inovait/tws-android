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

package si.inova.tws.web_socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.web_socket.SnippetWebSocketListener.Companion.CLOSING_CODE_ERROR_CODE
import si.inova.tws.web_socket.model.ActionType
import si.inova.tws.web_socket.model.SnippetUpdateAction
import timber.log.Timber

/**
 *
 * Creation of The Web Snippet websocket
 *
 */
class TwsSocket(scope: CoroutineScope) {

   private var webSocket: WebSocket? = null

   private val listener = SnippetWebSocketListener()

   private val _snippetsFlow: MutableStateFlow<List<WebSnippetData>> = MutableStateFlow(emptyList())
   val snippetsFlow: Flow<List<WebSnippetData>> = _snippetsFlow.filterNotNull()

   init {
      scope.launch {
         listener.updateActionFlow.collect {
            updateWithUpdateAction(it)
         }
      }
   }

   /**
    *
    * set at the start what your [_snippetsFlow] should look like before of socket triggering
    *
    *  @param data to update [_snippetsFlow]
    */
   fun manuallyUpdateSnippet(data: List<WebSnippetData>) {
      _snippetsFlow.update { data }
   }

   /**
    * Sets the URL target of this request.
    *
    * @throws IllegalArgumentException if [url] is not a valid HTTP or HTTPS URL. Avoid this
    *     exception by calling [HttpUrl.parse]; it returns null for invalid URLs.
    */
   fun setupWebSocketConnection(wwsUrl: String) {
      try {
         val client = OkHttpClient()
         val request = Request.Builder().url(wwsUrl).build()

         webSocket = client.newWebSocket(request, listener)
         client.dispatcher.executorService.shutdown()
      } catch (e: Exception) {
         Timber.e("Websocket error", e)
      }
   }

   /**
    * Attempts to initiate a graceful shutdown of this web socket.
    *
    * This returns true if a graceful shutdown was initiated by this call. It returns false if
    * a graceful shutdown was already underway or if the web socket is already closed or canceled.
    *
    */
   fun closeWebsocketConnection(): Boolean? {
      return webSocket?.close(CLOSING_CODE_ERROR_CODE, null).apply {
         webSocket = null
      }
   }

   /**
    * Returns `true` if websocket exists
    */
   fun socketExists(): Boolean {
      return webSocket != null
   }

   private fun updateWithUpdateAction(action: SnippetUpdateAction) {
      when (action.type) {
         ActionType.CREATED -> {
            _snippetsFlow.update { data ->
               data.toMutableList().apply {
                  if (action.data.target != null) {
                     add(
                        WebSnippetData(
                           id = action.data.id,
                           url = action.data.target,
                           headers = action.data.headers ?: emptyMap()
                        )
                     )
                  }
               }
            }
         }

         ActionType.UPDATED -> {
            _snippetsFlow.update { data ->
               data.map {
                  if (it.id == action.data.id) {
                     it.copy(
                        loadIteration = it.loadIteration + 1,
                        url = action.data.target ?: it.url,
                        headers = action.data.headers ?: it.headers
                     )
                  } else {
                     it
                  }
               }
            }
         }

         ActionType.DELETED -> {
            _snippetsFlow.update { data ->
               data.filter { it.id != action.data.id }
            }
         }
      }
   }
}
