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

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import si.inova.tws.web_socket.data.SnippetUpdateAction

internal class SnippetWebSocketListener : WebSocketListener() {
   private val _updateActionFlow: MutableStateFlow<SnippetUpdateAction?> = MutableStateFlow(null)
   val updateActionFlow: Flow<SnippetUpdateAction>
      get() = _updateActionFlow.filterNotNull()

   private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

   override fun onMessage(webSocket: WebSocket, text: String) {
      val adapter = moshi.adapter(SnippetUpdateAction::class.java)
      val snippetAction = adapter.fromJson(text)

      _updateActionFlow.update { snippetAction }
   }

   override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
      super.onClosing(webSocket, code, reason)
      webSocket.close(CLOSING_CODE_ERROR_CODE, null)
   }

   override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
      super.onFailure(webSocket, t, response)
      webSocket.close(CLOSING_CODE_ERROR_CODE, null)
   }

   companion object {
      const val CLOSING_CODE_ERROR_CODE = 1000
   }
}
