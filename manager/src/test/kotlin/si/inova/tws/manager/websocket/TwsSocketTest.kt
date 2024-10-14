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
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.testMainImmediateBackgroundScope
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.utils.CREATE_SNIPPET

@OptIn(ExperimentalCoroutinesApi::class)
class TwsSocketTest {
    private val scope = TestScopeWithDispatcherProvider()

    private lateinit var twsSocket: TwsSocket
    private lateinit var mockWebSocket: WebSocket
    private lateinit var mockListener: SnippetWebSocketListener

    @Before
    fun setUp() {
        mockWebSocket = mock(WebSocket::class.java)
        mockListener = SnippetWebSocketListener()
        mockkStatic(Log::class)
        every { Log.i(any(), any(), any()) } returns 0

        twsSocket = TwsSocketImpl(scope = scope.testMainImmediateBackgroundScope)
    }

    @Test
    fun `test setupWebSocketConnection opens socket`() = scope.runTest {
        val testUrl = "wss://example.com/socket"

        twsSocket.setupWebSocketConnection(testUrl)

        assertTrue(twsSocket.connectionExists())
    }

    @Test
    fun `test closeWebSocketConnection closes socket`() = scope.runTest {
        twsSocket.setupWebSocketConnection("wss://example.com/socket")

        assertTrue(twsSocket.connectionExists())

        twsSocket.closeWebsocketConnection()

        assertTrue(twsSocket.connectionExists().not())
    }

    @Test
    fun `test websocket receives message`() = scope.runTest {
        twsSocket.setupWebSocketConnection("wss://example.com/socket")
        mockListener.onMessage(mockWebSocket, CREATE_SNIPPET)

        val actions = mutableListOf<SnippetUpdateAction>()
        val job = launch {
            mockListener.updateActionFlow.collect { action ->
                actions.add(action)
            }
        }
        runCurrent()

        assertTrue(actions.isNotEmpty())

        job.cancel()
    }
}
