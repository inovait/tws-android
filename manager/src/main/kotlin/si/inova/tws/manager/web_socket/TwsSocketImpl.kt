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

package si.inova.tws.manager.web_socket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.web_socket.SnippetWebSocketListener.Companion.CLOSING_CODE_ERROR_CODE

/**
 *
 * Creation of The Web Snippet websocket
 *
 */
class TwsSocketImpl(scope: CoroutineScope) : TwsSocket {
    private val listener = SnippetWebSocketListener()

    private var webSocket: WebSocket? = null
    private var wssUrl: String? = null

    override val updateActionFlow = listener.updateActionFlow

    init {
        scope.launch {
            listener.socketStatus.collect { status ->
                when (status) {
                    is WebSocketStatus.Failed -> {
                        if (status.response?.code != 403) {
                            wssUrl?.let {
                                setupWebSocketConnection(it)
                                delay(RECONNECT_DELAY)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if [setupWssUrl] is not a valid HTTP or HTTPS URL. Avoid this
     *     exception by calling [HttpUrl.parse]; it returns null for invalid URLs.
     */
    override fun setupWebSocketConnection(setupWssUrl: String) {
        if (wssUrl != null) {
            // wss url changed, close previous and open new
            closeWebsocketConnection()
        }

        wssUrl = setupWssUrl

        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(setupWssUrl).build()

            webSocket = client.newWebSocket(request, listener)
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG_ERROR_WEBSOCKET, e.message, e)
        }
    }

    /**
     * Attempts to initiate a graceful shutdown of this web socket.
     *
     * This returns true if a graceful shutdown was initiated by this call. It returns false if
     * a graceful shutdown was already underway or if the web socket is already closed or canceled.
     *
     */
    override fun closeWebsocketConnection(): Boolean? {
        return webSocket?.close(CLOSING_CODE_ERROR_CODE, null).apply {
            wssUrl = null
            webSocket = null
        }
    }


    /**
     *
     * Check if connections exists
     *
     */
    override fun connectionExists(): Boolean = webSocket != null

    companion object {
        private const val TAG_ERROR_WEBSOCKET = "WebsocketError"
        private const val RECONNECT_DELAY = 5000L
    }
}
