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
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWith
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.tws.data.DynamicResourceDto
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.utils.FAKE_SNIPPET_FIVE
import si.inova.tws.manager.utils.FAKE_SNIPPET_FOUR
import si.inova.tws.manager.utils.FAKE_SNIPPET_ONE
import si.inova.tws.manager.utils.FAKE_SNIPPET_THREE
import si.inova.tws.manager.utils.FAKE_SNIPPET_TWO
import si.inova.tws.manager.utils.FakeCacheManager
import si.inova.tws.manager.utils.FakeLocalSnippetHandler
import si.inova.tws.manager.utils.FakeTWSSocket
import si.inova.tws.manager.utils.FakeTWSFunctions
import si.inova.tws.manager.utils.toActionBody

@OptIn(ExperimentalCoroutinesApi::class)
class TWSManagerImplTest {
    private val scope = TestScopeWithDispatcherProvider()

    private val functions = FakeTWSFunctions()
    private val socket = FakeTWSSocket()
    private val handler = FakeLocalSnippetHandler()
    private val cache = FakeCacheManager()

    private lateinit var webSnippetManager: TWSManagerImpl

    @Before
    fun setUp() {
        webSnippetManager = TWSManagerImpl(
            context = mock(),
            configuration = TWSConfiguration.Basic("organization", "project", "apiKey"),
            tag = "TestManager",
            scope = scope.backgroundScope,
            functions = functions,
            twsSocket = socket,
            localSnippetHandler = handler,
            cacheManager = cache,
        )
        cache.clear()
    }

    @Test
    fun `Loading snippets with project and organization id`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Loading shared snippet with shared id`() = scope.runTest {
        webSnippetManager = TWSManagerImpl(
            context = mock(),
            configuration = TWSConfiguration.Shared("shared", "apiKey"),
            tag = "TestManager",
            scope = scope.backgroundScope,
            functions = functions,
            twsSocket = socket,
            localSnippetHandler = handler,
            cacheManager = cache
        )

        functions.returnedProject = FAKE_PROJECT_DTO
        functions.returnedSharedSnippet = FAKE_SHARED_PROJECT

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }

        webSnippetManager.mainSnippetIdFlow.test {
            runCurrent()
            assert(expectMostRecentItem() == FAKE_SNIPPET_ONE.id)
        }
    }

    @Test
    fun `Load snippets and delete one from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))
        }
    }

    @Test
    fun `Load snippets and update tab target from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.example.com")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(target = "www.example.com"),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and create snippet from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.CREATED,
                    FAKE_SNIPPET_THREE.toActionBody()
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE,
                    FAKE_SNIPPET_THREE
                )
            )
        }
    }

    @Test
    fun `Load snippets and create, update and delete from web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.CREATED,
                    FAKE_SNIPPET_THREE.toActionBody()
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE,
                    FAKE_SNIPPET_THREE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.updated.com")
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(target = "www.updated.com"),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE,
                    FAKE_SNIPPET_THREE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.DELETED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id)
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE,
                    FAKE_SNIPPET_THREE
                )
            )
        }
    }

    @Test
    fun `Load snippets and delete snippet from local handler`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))
        }
    }

    @Test
    fun `Load snippets and and update from socket with html changes`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.UPDATED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(loadIteration = 1),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and delete from local handler and web socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO.copy(
            snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE)
        )

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
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

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            handler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE))

            socket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))

            expectNoEvents() // There should be no changes, since deleted snippet was already deleted
        }
    }

    @Test
    fun `Load content snippets from cache if available and fetch from api`() = scope.runTest {
        cache.save(TWSManagerImpl.CACHED_SNIPPETS, listOf(FAKE_SNIPPET_ONE))
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.snippetsFlow.test {
            awaitItem() // initial empty progress

            webSnippetManager.run()

            val progress = awaitItem() // progress with cached items
            progress.shouldBeProgressWith(listOf(FAKE_SNIPPET_ONE))

            val success = awaitItem() // success with network items
            success.shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Update dynamic resources with socket`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            socket.mockUpdateAction(
                SnippetUpdateAction(
                    type = ActionType.UPDATED,
                    data = ActionBody(
                        id = FAKE_SNIPPET_ONE.id,
                        dynamicResources = listOf(DynamicResourceDto("https://test.cs", "text/css"))
                    )
                )
            )

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(
                        dynamicResources = listOf(DynamicResourceDto("https://test.cs", "text/css"))
                    ),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            expectNoEvents()
        }
    }

    @Test
    fun `Setting local props to snippet should insert props to snippet`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))

            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(props = FAKE_SNIPPET_ONE.props + mapOf("name" to "Chris")),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Setting local props multiple times to snippet should insert props to snippet`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            webSnippetManager.setLocalProps(
                FAKE_SNIPPET_ONE.id,
                mapOf(
                    "name" to "Chris",
                    "surname" to "Donovan"
                )
            )

            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(props = FAKE_SNIPPET_ONE.props + mapOf("name" to "Chris", "surname" to "Donovan")),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Setting local props with multiple values to snippet should override props to snippet`() = scope.runTest {
        functions.returnedProject = FAKE_PROJECT_DTO

        webSnippetManager.run()

        webSnippetManager.snippetsFlow.test {
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE,
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )

            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))
            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("surname" to "Donovan"))

            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_SNIPPET_ONE.copy(props = FAKE_SNIPPET_ONE.props + mapOf("surname" to "Donovan")),
                    FAKE_SNIPPET_TWO,
                    FAKE_SNIPPET_FOUR,
                    FAKE_SNIPPET_FIVE
                )
            )
        }
    }
}
