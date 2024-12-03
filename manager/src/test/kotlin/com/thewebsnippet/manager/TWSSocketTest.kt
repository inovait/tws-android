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

package com.thewebsnippet.manager

import app.cash.turbine.test
import com.thewebsnippet.manager.data.WebSocketStatus
import com.thewebsnippet.manager.fakes.FakeTWSSocketListener
import com.thewebsnippet.manager.utils.ADD_FAKE_SNIPPET_SOCKET
import com.thewebsnippet.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import com.thewebsnippet.manager.websocket.TWSSocket
import com.thewebsnippet.manager.websocket.TWSSocketImpl
import com.thewebsnippet.manager.websocket.TWSSocketListenerImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TWSSocketTest {
    private val scope = testScopeWithDispatcherProvider()

    private val fakeSocketListener = FakeTWSSocketListener()

    private val mockWebSocket = mockk<WebSocket>(relaxed = true)
    private val client = mockk<OkHttpClient>(relaxed = true)

    private lateinit var twsSocket: TWSSocket

    @Before
    fun setUp() {
        twsSocket = TWSSocketImpl(
            scope = scope.backgroundScope,
            listener = fakeSocketListener,
            client = client
        )

        every { client.newWebSocket(any(), fakeSocketListener) } returns mockWebSocket
        every { mockWebSocket.close(any(), any()) } returns true
    }

    @Test
    fun `setupWebSocketConnection should open socket`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }

        verify { client.newWebSocket(any(), fakeSocketListener) }
    }

    @Test
    fun `setupWebSocketConnection should not reconnect if URL is same`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }
        twsSocket.setupWebSocketConnection(testUrl) { }

        verify(exactly = 1) { client.newWebSocket(any(), fakeSocketListener) }
    }

    @Test
    fun `setupWebSocketConnection should close and reopen connection if new URL`() = scope.runTest {
        val testUrl = "wss://example.com/socket"
        val testUrl1 = "wss://example.com/socket1"

        twsSocket.setupWebSocketConnection(testUrl) { }
        twsSocket.setupWebSocketConnection(testUrl1) { }

        verify(exactly = 1) { mockWebSocket.close(any(), any()) } // Check that the old connection was closed
        verify(exactly = 2) { client.newWebSocket(any(), any()) } // Check that new connection was opened for each URL
    }

    @Test
    fun `closeWebsocketConnection should close socket`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }
        twsSocket.closeWebsocketConnection()

        verify(exactly = 1) { client.newWebSocket(any(), any()) }
        verify(exactly = 1) { mockWebSocket.close(TWSSocketListenerImpl.CLOSING_CODE_ERROR_CODE, null) }
    }

    @Test
    fun `unauthorized error in WebSocketStatus should trigger unauthorizedCallback`() = scope.runTest {
        val testUrl = "wss://example.com/socket"
        var callbackCalled = false

        twsSocket.setupWebSocketConnection(testUrl) {
            callbackCalled = true
        }

        fakeSocketListener.mockSocketStatus(WebSocketStatus.Failed(401))
        runCurrent()

        assert(callbackCalled)
    }

    @Test
    fun `internal error in WebSocketStatus should not trigger unauthorizedCallback`() = scope.runTest {
        val testUrl = "wss://example.com/socket"
        var callbackCalled = false

        twsSocket.setupWebSocketConnection(testUrl) {
            callbackCalled = true
        }

        fakeSocketListener.mockSocketStatus(WebSocketStatus.Failed(500))
        runCurrent()

        assert(!callbackCalled)
    }

    @Test
    fun `setup new connection after initial has been closed due to failure`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }

        verify(exactly = 1) { // first setup
            client.newWebSocket(any(), fakeSocketListener)
        }

        fakeSocketListener.mockSocketStatus(WebSocketStatus.Failed(408))

        advanceTimeBy(4_000)

        verify(exactly = 1) { // first setup, retry delay has not passed yet
            client.newWebSocket(any(), fakeSocketListener)
        }

        advanceTimeBy(2_000)

        verify(exactly = 2) { // first setup and retry due to failure
            client.newWebSocket(any(), fakeSocketListener)
        }
    }

    @Test
    fun `forbidden error in WebSocketStatus should prevent reconnect attempts`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }

        verify(exactly = 1) { // first setup
            client.newWebSocket(any(), fakeSocketListener)
        }

        fakeSocketListener.mockSocketStatus(WebSocketStatus.Failed(403))

        advanceTimeBy(6_000)

        verify(exactly = 1) { // first setup
            client.newWebSocket(any(), fakeSocketListener)
        }
    }

    @Test
    fun `socket actions should be exposed`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl) { }

        twsSocket.updateActionFlow.test {
            fakeSocketListener.mockUpdateActionFlow(ADD_FAKE_SNIPPET_SOCKET)

            assert(expectMostRecentItem() == ADD_FAKE_SNIPPET_SOCKET)

            fakeSocketListener.mockUpdateActionFlow(UPDATED_FAKE_SNIPPET_SOCKET)

            assert(expectMostRecentItem() == UPDATED_FAKE_SNIPPET_SOCKET)
        }
    }
}
