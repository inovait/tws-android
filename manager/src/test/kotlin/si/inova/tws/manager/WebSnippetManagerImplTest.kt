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
import si.inova.tws.manager.data.SnippetType
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.utils.FAKE_SNIPPET_FIVE
import si.inova.tws.manager.utils.FAKE_SNIPPET_FOUR
import si.inova.tws.manager.utils.FAKE_SNIPPET_ONE
import si.inova.tws.manager.utils.FAKE_SNIPPET_SIX
import si.inova.tws.manager.utils.FAKE_SNIPPET_THREE
import si.inova.tws.manager.utils.FAKE_SNIPPET_TWO
import si.inova.tws.manager.utils.FakeLocalSnippetHandler
import si.inova.tws.manager.utils.FakeTwsSocket
import si.inova.tws.manager.utils.FakeWebSnippetFunction
import si.inova.tws.manager.utils.toActionBody

@OptIn(ExperimentalCoroutinesApi::class)
class WebSnippetManagerImplTest {
    private val scope = TestScopeWithDispatcherProvider()

    private val functions = FakeWebSnippetFunction()
    private val socket = FakeTwsSocket()
    private val handler = FakeLocalSnippetHandler()

    private lateinit var webSnippetManager: WebSnippetManagerImpl

    @Before
    fun setUp() {
        webSnippetManager = WebSnippetManagerImpl(
            context = mock(),
            webSnippetFunction = functions,
            resources = scope.testCoroutineResourceManager(),
            twsSocket = socket,
            localSnippetHandler = handler
        )
    }

    @Test
    fun `Loading snippets with project and organization id`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))
        }

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))
        }
    }

    @Test
    fun `Loading shared snippet with shared id`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO
        functions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        webSnippetManager.loadSharedSnippetData("shared")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))
        }

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))
        }

        webSnippetManager.mainSnippetIdFlow.test {
            runCurrent()
            assert(expectMostRecentItem() == FAKE_SNIPPET_ONE.id)
        }
    }

    @Test
    fun `Load snippets and delete tab from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO))
        }
    }

    @Test
    fun `Load snippets and delete popup from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))
        }

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_FOUR.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FIVE))
        }
    }

    @Test
    fun `Load snippets and update tab target from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
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
    fun `Load snippets and update popup target from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_FOUR.id, target = "www.example.com")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_FOUR.copy(target = "www.example.com", loadIteration = 1),
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and update html tab from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
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
    fun `Load snippets and update html popup from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_FOUR.id, html = "<script></script>>", type = SnippetType.POPUP)
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_FOUR.copy(html = "<script></script>>", loadIteration = 1),
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and create tab from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(
                ActionType.CREATED,
                FAKE_SNIPPET_THREE.toActionBody())
            )

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
        }
    }

    @Test
    fun `Load snippets and create popup from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            socket.mockUpdateAction(SnippetUpdateAction(
                ActionType.CREATED,
                FAKE_SNIPPET_SIX.toActionBody())
            )

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE, FAKE_SNIPPET_SIX))
        }
    }

    @Test
    fun `Load snippets and create, update and delete from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(
                ActionType.CREATED,
                FAKE_SNIPPET_THREE.toActionBody())
            )

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.updated.com")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(target = "www.updated.com", loadIteration = 1),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_THREE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.DELETED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id)
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
        }
    }

    @Test
    fun `Load snippets and delete tab from local handler`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO))
        }
    }

    @Test
    fun `Load snippets and delete popup from local handler`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.popupSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_FOUR.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_FIVE))
        }
    }


    @Test
    fun `Load snippets and delete from local handler and web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO.copy(
            snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE)
        )

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_TWO.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_THREE))
        }
    }

    @Test
    fun `Load snippets and delete same snippet from local handler and web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.loadWebSnippets("organization", "project")

        webSnippetManager.contentSnippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO))

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectNoEvents() // There should be no changes, since deleted snippet was already deleted
        }
    }
}
