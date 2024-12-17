/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thewebsnippet.manager.websocket

import android.util.Log
import com.thewebsnippet.manager.data.WebSocketStatus
import com.thewebsnippet.manager.websocket.TWSSocketListenerImpl.Companion.CLOSING_CODE_ERROR_CODE
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
