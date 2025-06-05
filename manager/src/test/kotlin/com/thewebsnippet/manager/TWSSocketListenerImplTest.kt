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
package com.thewebsnippet.manager

import android.util.Log
import app.cash.turbine.test
import com.thewebsnippet.manager.domain.model.WebSocketStatus
import com.thewebsnippet.manager.utils.ADD_FAKE_SNIPPET_SOCKET
import com.thewebsnippet.manager.utils.CREATE_SNIPPET
import com.thewebsnippet.manager.utils.DELETE_FAKE_SNIPPET_SOCKET
import com.thewebsnippet.manager.utils.DELETE_SNIPPET
import com.thewebsnippet.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET
import com.thewebsnippet.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET_HTML
import com.thewebsnippet.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET_PROPS
import com.thewebsnippet.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET_URL
import com.thewebsnippet.manager.utils.UPDATE_SNIPPET_DYNAMIC_RESOURCES
import com.thewebsnippet.manager.utils.UPDATE_SNIPPET_HTML
import com.thewebsnippet.manager.utils.UPDATE_SNIPPET_PROPS
import com.thewebsnippet.manager.utils.UPDATE_SNIPPET_URL
import com.thewebsnippet.manager.data.websocket.TWSSocketListenerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test

class TWSSocketListenerImplTest {
    private val mockWebSocket = mockk<WebSocket>(relaxed = true)
    private val mockResponse = mockk<Response>(relaxed = true)

    private lateinit var listener: TWSSocketListenerImpl

    @Before
    fun setUp() {
        listener = TWSSocketListenerImpl()

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.i(any(), any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun `onMessage should parse and emit SnippetUpdateAction`() = runTest {
        listener.updateActionFlow.test {
            listener.onMessage(mockWebSocket, CREATE_SNIPPET)
            assert(expectMostRecentItem() == ADD_FAKE_SNIPPET_SOCKET)

            listener.onMessage(mockWebSocket, UPDATE_SNIPPET_DYNAMIC_RESOURCES)
            assert(expectMostRecentItem() == UPDATED_FAKE_SNIPPET_SOCKET)

            listener.onMessage(mockWebSocket, UPDATE_SNIPPET_PROPS)
            assert(expectMostRecentItem() == UPDATED_FAKE_SNIPPET_SOCKET_PROPS)

            listener.onMessage(mockWebSocket, UPDATE_SNIPPET_URL)
            assert(expectMostRecentItem() == UPDATED_FAKE_SNIPPET_SOCKET_URL)

            listener.onMessage(mockWebSocket, UPDATE_SNIPPET_HTML)
            assert(expectMostRecentItem() == UPDATED_FAKE_SNIPPET_SOCKET_HTML)

            listener.onMessage(mockWebSocket, DELETE_SNIPPET)
            assert(expectMostRecentItem() == DELETE_FAKE_SNIPPET_SOCKET)
        }
    }

    @Test
    fun `onMessage with invalid message should not emit anything`() = runTest {
        listener.updateActionFlow.test {
            listener.onMessage(mockWebSocket, "corrupted message")
            expectNoEvents()
        }
    }

    @Test
    fun `onClosing should close WebSocket with correct code`() = runTest {
        listener.socketStatus.test {
            listener.onClosing(mockWebSocket, 1000, "Test reason")

            verify { mockWebSocket.close(TWSSocketListenerImpl.CLOSING_CODE_ERROR_CODE, null) }
        }
    }

    @Test
    fun `onClosed should emit Closed status`() = runTest {
        listener.socketStatus.test {
            listener.onClosed(mockWebSocket, 1000, "Closed normally")

            assert(expectMostRecentItem() == WebSocketStatus.Closed)
        }
    }

    @Test
    fun `onFailure should emit Failed status with correct response code`() = runTest {
        every { mockResponse.code } returns 500

        listener.socketStatus.test {
            listener.onFailure(mockWebSocket, Exception("Test Exception"), mockResponse)

            val status = expectMostRecentItem()
            assert(status is WebSocketStatus.Failed && status.code == 500)
        }
    }

    @Test
    fun `onOpen should emit Open status`() = runTest {
        listener.socketStatus.test {
            listener.onOpen(mockWebSocket, mockResponse)

            assert(expectMostRecentItem() == WebSocketStatus.Open)
        }
    }
}
