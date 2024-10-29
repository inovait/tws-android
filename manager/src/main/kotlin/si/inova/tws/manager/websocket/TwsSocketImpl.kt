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

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.service.NetworkConnectivityServiceImpl
import si.inova.tws.manager.websocket.SnippetWebSocketListener.Companion.CLOSING_CODE_ERROR_CODE

/**
 *
 * Creation of The Web Snippet websocket
 *
 */
class TwsSocketImpl(
    context: Context,
    private val scope: CoroutineScope,
    private val networkConnectivityService: NetworkConnectivityService = NetworkConnectivityServiceImpl(context),
    private val listener: TWSSocketListener = SnippetWebSocketListener()
) : TwsSocket {
    private var webSocket: WebSocket? = null
    private var wssUrl: String? = null

    private var isNetworkCollectorActive = false
    private var isSocketCollectorActive = false

    override val updateActionFlow = listener.updateActionFlow

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if [setupWssUrl] is not a valid HTTP or HTTPS URL. Avoid this
     *     exception by calling [HttpUrl.parse]; it returns null for invalid URLs.
     */
    override fun setupWebSocketConnection(setupWssUrl: String, unauthorizedCallback: () -> Unit) {
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

            setupSocketErrorHandling(unauthorizedCallback)
            setupNetworkConnectivityHandling(unauthorizedCallback)
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
            webSocket = null
        }
    }

    private fun reconnect(unauthorizedCallback: () -> Unit) {
        if (webSocket != null) {
            wssUrl?.let {
                setupWebSocketConnection(it, unauthorizedCallback)
            }
        }
    }

    private fun setupNetworkConnectivityHandling(unauthorizedCallback: () -> Unit) = scope.launch {
        if (isNetworkCollectorActive) return@launch
        isNetworkCollectorActive = true

        try {
            networkConnectivityService.networkStatus.collect { status ->
                when (status) {
                    is NetworkStatus.Connected -> {
                        reconnect(unauthorizedCallback)
                    }

                    is NetworkStatus.Disconnected -> {
                        closeWebsocketConnection()
                    }
                }
            }
        } finally {
            isNetworkCollectorActive = false
        }
    }

    private fun setupSocketErrorHandling(unauthorizedCallback: () -> Unit) = scope.launch {
        if (isSocketCollectorActive) return@launch
        isSocketCollectorActive = true

        var failedSocketReconnect = 0

        try {
            listener.socketStatus.collect { status ->
                when {
                    status is WebSocketStatus.Failed && status.response?.code == ERROR_UNAUTHORIZED -> {
                        unauthorizedCallback()
                        return@collect
                    }

                    status is WebSocketStatus.Failed && failedSocketReconnect < MAXIMUM_RETRIES -> {
                        if (status.response?.code != ERROR_FORBIDDEN) {
                            delay(RECONNECT_DELAY)
                            failedSocketReconnect++
                            wssUrl?.let { setupWebSocketConnection(it, unauthorizedCallback) }
                        }
                    }

                    status is WebSocketStatus.Open -> failedSocketReconnect = 0
                }
            }
        } finally {
            isSocketCollectorActive = false
        }
    }

    companion object {
        private const val TAG_ERROR_WEBSOCKET = "WebsocketError"
        private const val RECONNECT_DELAY = 5000L
        private const val MAXIMUM_RETRIES = 5
        private const val ERROR_UNAUTHORIZED = 401
        private const val ERROR_FORBIDDEN = 403
    }
}
