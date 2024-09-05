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

package si.inova.tws.manager

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.SnippetUpdateAction

@OptIn(ExperimentalCoroutinesApi::class)
class WebSnippetManagerImplTest {
    private val scope = TestScopeWithDispatcherProvider()

    private val functions = FakeWebSnippetFunction()
    private val socket = FakeTwsSocket()

    private lateinit var webSnippetManager: WebSnippetManagerImpl

    @Before
    fun setUp() {
        webSnippetManager = WebSnippetManagerImpl(
            context = mock(),
            webSnippetFunction = functions,
            resources = scope.testCoroutineResourceManager(),
            twsSocket = socket
        )
    }

    @Test
    fun `Loading snippets with project and organization id`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.snippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))
        }
    }

    @Test
    fun `Loading shared snippet with shared id`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO
        functions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        webSnippetManager.loadSharedSnippetData("shared")

        webSnippetManager.snippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))
        }

        webSnippetManager.mainSnippetIdFlow.test {
            runCurrent()
            assert(expectMostRecentItem() == FAKE_SNIPPET_ONE.id)
        }
    }

    @Test
    fun `Load snippets and delete from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO))
        }
    }

    @Test
    fun `Load snippets and update target from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.example.com")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(target = "www.example.com", loadIteration = 1),
                    FAKE_SNIPPET_TWO
                )
            )
        }
    }

    @Test
    fun `Load snippets and update html from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, html = "<script></script>>")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(html = "<script></script>>", loadIteration = 1),
                    FAKE_SNIPPET_TWO
                )
            )
        }
    }

    @Test
    fun `Load snippets and create from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(
                ActionType.CREATED,
                FAKE_SNIPPET_THREE.toActionBody())
            )

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
        }
    }
}
