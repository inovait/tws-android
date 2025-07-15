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
package com.thewebsnippet.manager.data.websocket

import android.util.Log
import com.thewebsnippet.manager.domain.model.WebSocketStatus
import com.thewebsnippet.manager.domain.websocket.TWSSocket
import com.thewebsnippet.manager.domain.websocket.TWSSocketListener
import com.thewebsnippet.manager.data.websocket.TWSSocketListenerImpl.Companion.CLOSING_CODE_ERROR_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

/**
 *
 * Creation of The Web Snippet websocket
 *
 */
internal class TWSSocketImpl(
    private val scope: CoroutineScope,
    private val listener: TWSSocketListener = TWSSocketListenerImpl(),
    private val client: OkHttpClient = OkHttpClient()
) : TWSSocket {
    private var webSocket: WebSocket? = null
    private var wssUrl: String? = null

    private var isSocketCollectorActive = false

    override val updateActionFlow = listener.updateActionFlow

    /**
     * Sets the URL target of this request.
     *
     * @throws IllegalArgumentException if [setupWssUrl] is not a valid HTTP or HTTPS URL. Avoid this
     *     exception by calling [HttpUrl.parse]; it returns null for invalid URLs.
     */
    override fun setupWebSocketConnection(setupWssUrl: String, unauthorizedCallback: suspend () -> Unit) {
        if (wssUrl == setupWssUrl && webSocket != null) {
            // socket already configured
            return
        }

        if (wssUrl != null && webSocket != null) {
            // wss url changed, close previous and open new
            disconnect()
        }

        wssUrl = setupWssUrl

        try {
            val request = Request.Builder().url(setupWssUrl).build()

            webSocket = client.newWebSocket(request, listener)

            setupSocketErrorHandling(unauthorizedCallback)
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
        wssUrl = null

        return disconnect()
    }

    private fun disconnect(): Boolean? {
        return webSocket?.close(CLOSING_CODE_ERROR_CODE, null).also {
            webSocket = null
        }
    }

    private fun setupSocketErrorHandling(unauthorizedCallback: suspend () -> Unit) {
        if (isSocketCollectorActive) return
        isSocketCollectorActive = true

        var failedSocketReconnect = 0

        listener.socketStatus.onEach { status ->
            if (status is WebSocketStatus.Failed) {
                // webSocket has been closed due to failure, we should clear it, to force new setup
                webSocket = null
            }

            when {
                status is WebSocketStatus.Failed && status.code == ERROR_UNAUTHORIZED -> {
                    unauthorizedCallback()
                }

                status is WebSocketStatus.Failed && status.code != ERROR_FORBIDDEN && failedSocketReconnect < MAXIMUM_RETRIES -> {
                    delay(RECONNECT_DELAY)
                    failedSocketReconnect++
                    wssUrl?.let { setupWebSocketConnection(it, unauthorizedCallback) }
                }

                status is WebSocketStatus.Open -> failedSocketReconnect = 0
            }
        }.launchIn(scope).invokeOnCompletion {
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
