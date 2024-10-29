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

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.testMainImmediateBackgroundScope
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.utils.ADD_FAKE_SNIPPET_SOCKET
import si.inova.tws.manager.utils.CREATE_SNIPPET
import si.inova.tws.manager.utils.FakeNetworkConnectivityService
import si.inova.tws.manager.utils.FakeTWSSocketListener
import si.inova.tws.manager.utils.UPDATED_FAKE_SNIPPET_SOCKET
import si.inova.tws.manager.utils.UPDATE_SNIPPET_DYNAMIC_RESOURCES

@OptIn(ExperimentalCoroutinesApi::class)
class TWSSocketTest {
    private val scope = TestScopeWithDispatcherProvider()

    private lateinit var twsSocket: TWSSocket
    private lateinit var mockWebSocket: WebSocket
    private lateinit var client: OkHttpClient
    private val socketListener = FakeTWSSocketListener()
    private val networkConnectivityService = FakeNetworkConnectivityService()

    @Before
    fun setUp() {
        mockWebSocket = mock(WebSocket::class.java)
        client = mock()

        twsSocket = TWSSocketImpl(
            context = mock(),
            scope = scope.testMainImmediateBackgroundScope,
            networkConnectivityService = networkConnectivityService,
            listener = socketListener,
            client = client
        )
    }

    @Test
    fun `test websocket receives message`() = scope.runTest {
        twsSocket.setupWebSocketConnection("wss://example.com/socket") {}
        socketListener.onMessage(mockWebSocket, CREATE_SNIPPET)

        val actions = mutableListOf<SnippetUpdateAction>()
        val job = launch {
            socketListener.updateActionFlow.collect { action ->
                actions.add(action)
            }
        }
        runCurrent()

        assertTrue(actions.contains(ADD_FAKE_SNIPPET_SOCKET))

        job.cancel()
    }

    @Test
    fun `update dynamic resources`() = scope.runTest {
        twsSocket.setupWebSocketConnection("wss://example.com/socket") {}
        socketListener.onMessage(mockWebSocket, CREATE_SNIPPET)

        val actions = mutableListOf<SnippetUpdateAction>()
        val job = launch {
            socketListener.updateActionFlow.collect { action ->
                actions.add(action)
            }
        }
        runCurrent()

        assertTrue(actions.contains(ADD_FAKE_SNIPPET_SOCKET))

        socketListener.onMessage(mockWebSocket, UPDATE_SNIPPET_DYNAMIC_RESOURCES)
        runCurrent()

        assertTrue(actions.contains(UPDATED_FAKE_SNIPPET_SOCKET))

        job.cancel()
    }
}
