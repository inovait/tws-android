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

package si.inova.tws.manager.websocket

import android.util.Log
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import okhttp3.Response
import okhttp3.WebSocket
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.singleton.twsMoshi

internal class SnippetWebSocketListener : TWSSocketListener() {
    private val _updateActionFlow: MutableStateFlow<SnippetUpdateAction?> = MutableStateFlow(null)
    override val updateActionFlow: Flow<SnippetUpdateAction>
        get() = _updateActionFlow.filterNotNull()

    private val moshi: Moshi by lazy { twsMoshi() }

    private val _socketStatus: MutableStateFlow<WebSocketStatus?> = MutableStateFlow(null)
    override val socketStatus: Flow<WebSocketStatus>
        get() = _socketStatus.filterNotNull()

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(TAG_SOCKET_STATUS, "SOCKET MESSAGE $text")

        val adapter = moshi.adapter(SnippetUpdateAction::class.java)
        val snippetAction = adapter.fromJson(text)

        _updateActionFlow.update { snippetAction }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.i(TAG_SOCKET_STATUS, "SOCKET CLOSING")

        webSocket.close(CLOSING_CODE_ERROR_CODE, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.i(TAG_SOCKET_STATUS, "SOCKET FAILED", t)

        _socketStatus.tryEmit(WebSocketStatus.Failed(response?.code))

        webSocket.close(CLOSING_CODE_ERROR_CODE, null)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(TAG_SOCKET_STATUS, "SOCKET OPEN")

        _socketStatus.tryEmit(WebSocketStatus.Open)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.i(TAG_SOCKET_STATUS, "SOCKET CLOSED")

        _socketStatus.tryEmit(WebSocketStatus.Closed)
    }

    companion object {
        const val CLOSING_CODE_ERROR_CODE = 1000

        private const val TAG_SOCKET_STATUS: String = "SocketStatus"
    }
}
