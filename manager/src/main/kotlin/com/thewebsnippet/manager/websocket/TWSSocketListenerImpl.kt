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
import com.squareup.moshi.Moshi
import com.thewebsnippet.manager.data.SnippetUpdateAction
import com.thewebsnippet.manager.data.WebSocketStatus
import com.thewebsnippet.manager.setup.twsMoshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import okhttp3.Response
import okhttp3.WebSocket

internal class TWSSocketListenerImpl : TWSSocketListener() {
    private val _updateActionFlow: MutableSharedFlow<SnippetUpdateAction> = MutableSharedFlow(replay = 1)
    override val updateActionFlow: Flow<SnippetUpdateAction>
        get() = _updateActionFlow.filterNotNull()

    private val moshi: Moshi by lazy { twsMoshi() }

    private val _socketStatus: MutableStateFlow<WebSocketStatus?> = MutableStateFlow(null)
    override val socketStatus: Flow<WebSocketStatus>
        get() = _socketStatus.filterNotNull()

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(TAG_SOCKET_STATUS, "SOCKET MESSAGE $text")

        try {
            val adapter = moshi.adapter(SnippetUpdateAction::class.java)
            val snippetAction = adapter.fromJson(text)

            snippetAction?.let {
                _updateActionFlow.tryEmit(it)
            }
        } catch (e: Exception) {
            Log.e(TAG_SOCKET_STATUS, e.message.orEmpty(), e)
        }
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
