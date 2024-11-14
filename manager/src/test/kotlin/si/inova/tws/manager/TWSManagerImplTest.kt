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

import android.content.Context
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWith
import si.inova.kotlinova.core.test.outcomes.shouldBeProgressWithData
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.tws.data.TWSAttachment
import si.inova.tws.manager.cache.CacheManager
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.NetworkStatus
import si.inova.tws.manager.data.ProjectDto
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.localhandler.LocalSnippetHandler
import si.inova.tws.manager.service.NetworkConnectivityService
import si.inova.tws.manager.snippet.ProjectResponse
import si.inova.tws.manager.snippet.SnippetLoadingManager
import si.inova.tws.manager.utils.FAKE_EXPOSED_SNIPPET_FIVE
import si.inova.tws.manager.utils.FAKE_EXPOSED_SNIPPET_FOUR
import si.inova.tws.manager.utils.FAKE_EXPOSED_SNIPPET_ONE
import si.inova.tws.manager.utils.FAKE_EXPOSED_SNIPPET_THREE
import si.inova.tws.manager.utils.FAKE_EXPOSED_SNIPPET_TWO
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO
import si.inova.tws.manager.utils.FAKE_PROJECT_DTO_2
import si.inova.tws.manager.utils.FAKE_SHARED_PROJECT
import si.inova.tws.manager.utils.FAKE_SNIPPET_FIVE
import si.inova.tws.manager.utils.FAKE_SNIPPET_FOUR
import si.inova.tws.manager.utils.FAKE_SNIPPET_ONE
import si.inova.tws.manager.utils.FAKE_SNIPPET_THREE
import si.inova.tws.manager.utils.FAKE_SNIPPET_TWO
import si.inova.tws.manager.utils.FakeCacheManager
import si.inova.tws.manager.utils.FakeLocalSnippetHandler
import si.inova.tws.manager.utils.FakeNetworkConnectivityService
import si.inova.tws.manager.utils.FakeSnippetLoadingManager
import si.inova.tws.manager.utils.FakeTWSSocket
import si.inova.tws.manager.utils.toActionBody
import si.inova.tws.manager.websocket.TWSSocket
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TWSManagerImplTest {
    private val fakeScope = TestScopeWithDispatcherProvider()

    private val fakeSocket = FakeTWSSocket()
    private val fakeHandler = FakeLocalSnippetHandler()
    private val fakeCache = FakeCacheManager()
    private val fakeLoader = FakeSnippetLoadingManager()
    private val fakeNetworkConnectivityService = FakeNetworkConnectivityService()

    private lateinit var webSnippetManager: TWSManager

    @Before
    fun setUp() {
        webSnippetManager = copyTWSManagerImpl()
        fakeCache.clear()
    }

    @Test
    fun `Loading snippets with project and organization id`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Loading shared snippet with shared id`() = fakeScope.runTest {
        webSnippetManager = copyTWSManagerImpl(configuration = TWSConfiguration.Shared("shared", "apiKey"))

        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN,
            FAKE_SHARED_PROJECT.snippet.id
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }

        webSnippetManager.mainSnippetIdFlow.test {
            runCurrent()
            assert(expectMostRecentItem() == FAKE_SHARED_PROJECT.snippet.id)
        }
    }

    @Test
    fun `Load snippets and delete one from web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and update tab target from web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeSocket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.example.com")
                )
            )
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(target = "www.example.com"),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and create snippet from web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeSocket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.CREATED,
                    FAKE_SNIPPET_THREE.toActionBody()
                )
            )
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE,
                    FAKE_EXPOSED_SNIPPET_THREE
                )
            )
        }
    }

    @Test
    fun `Load snippets and create, update and delete from web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(FAKE_PROJECT_DTO, Instant.MIN)

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.CREATED, FAKE_SNIPPET_THREE.toActionBody()))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE,
                    FAKE_EXPOSED_SNIPPET_THREE
                )
            )

            fakeSocket.mockUpdateAction(
                SnippetUpdateAction(
                    ActionType.UPDATED,
                    ActionBody(id = FAKE_SNIPPET_ONE.id, target = "www.updated.com")
                )
            )
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(target = "www.updated.com"),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE,
                    FAKE_EXPOSED_SNIPPET_THREE
                )
            )

            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE,
                    FAKE_EXPOSED_SNIPPET_THREE
                )
            )
        }
    }

    @Test
    fun `Load snippets and delete snippet from local handler`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeHandler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(FAKE_EXPOSED_SNIPPET_TWO, FAKE_EXPOSED_SNIPPET_FOUR, FAKE_EXPOSED_SNIPPET_FIVE)
            )
        }
    }

    @Test
    fun `Load snippets and and update from socket with html changes`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeHandler.mockUpdateAction(SnippetUpdateAction(ActionType.UPDATED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(loadIteration = 1),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Load snippets and delete from local handler and web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO.copy(
                snippets = listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE)
            ),
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(FAKE_EXPOSED_SNIPPET_ONE, FAKE_EXPOSED_SNIPPET_TWO, FAKE_EXPOSED_SNIPPET_THREE)
            )

            fakeHandler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_EXPOSED_SNIPPET_TWO, FAKE_EXPOSED_SNIPPET_THREE))

            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_TWO.id)))
            runCurrent()
            expectMostRecentItem().shouldBeSuccessWithData(listOf(FAKE_EXPOSED_SNIPPET_THREE))
        }
    }

    @Test
    fun `Load snippets and delete same snippet from local handler and web socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeHandler.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(FAKE_EXPOSED_SNIPPET_TWO, FAKE_EXPOSED_SNIPPET_FOUR, FAKE_EXPOSED_SNIPPET_FIVE)
            )

            fakeSocket.mockUpdateAction(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
            runCurrent()

            expectNoEvents() // There should be no changes, since deleted snippet was already deleted
        }
    }

    @Test
    fun `Load content snippets from cache if available and fetch from api`() = fakeScope.runTest {
        fakeCache.save(TWSManagerImpl.CACHED_SNIPPETS, listOf(FAKE_SNIPPET_ONE))

        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            val progress = awaitItem() // progress with cached items
            progress.shouldBeProgressWith(listOf(FAKE_EXPOSED_SNIPPET_ONE))

            val success = awaitItem() // success with network items
            success.shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeLoader.loaderResponse = ProjectResponse(
                ProjectDto(
                    snippets = listOf(FAKE_SNIPPET_ONE),
                    listenOn = "wss:someUrl.com"
                ),
                Instant.MIN
            )

            webSnippetManager.forceRefresh()

            awaitItem().shouldBeProgressWith(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            awaitItem().shouldBeSuccessWithData(listOf(FAKE_EXPOSED_SNIPPET_ONE))
        }
    }

    @Test
    fun `Update dynamic resources with socket`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            fakeSocket.mockUpdateAction(
                SnippetUpdateAction(
                    type = ActionType.UPDATED,
                    data = ActionBody(
                        id = FAKE_SNIPPET_ONE.id,
                        dynamicResources = listOf(TWSAttachment("https://test.cs", "text/css"))
                    )
                )
            )
            runCurrent()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(
                        dynamicResources = listOf(TWSAttachment("https://test.cs", "text/css"))
                    ),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            expectNoEvents()
        }
    }

    @Test
    fun `Setting local props to snippet should insert props to snippet`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))

            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(props = FAKE_SNIPPET_ONE.props + mapOf("name" to "Chris")),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Setting local props multiple times to snippet should insert props to snippet`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
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
                    FAKE_EXPOSED_SNIPPET_ONE.copy(
                        props = FAKE_EXPOSED_SNIPPET_ONE.props + mapOf("name" to "Chris", "surname" to "Donovan")
                    ),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Setting local props with multiple values to snippet should override props to snippet`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))
            webSnippetManager.setLocalProps(FAKE_SNIPPET_ONE.id, mapOf("surname" to "Donovan"))

            runCurrent()

            expectMostRecentItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(props = FAKE_EXPOSED_SNIPPET_ONE.props + mapOf("surname" to "Donovan")),
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Loading already cached shared snippet with shared id`() = fakeScope.runTest {
        fakeCache.save(
            TWSManagerImpl.CACHED_SNIPPETS,
            listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_FOUR, FAKE_SNIPPET_FIVE)
        )

        webSnippetManager = copyTWSManagerImpl(configuration = TWSConfiguration.Shared("shared", "apiKey"))

        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN,
            FAKE_SHARED_PROJECT.snippet.id
        )

        webSnippetManager.forceRefresh()

        webSnippetManager.snippets.test {
            awaitItem().shouldBeProgressWith() // initial emit

            awaitItem().shouldBeProgressWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
            runCurrent()
            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    @Test
    fun `Reload snippets if network connection lost and reestablished`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets.test {
            runCurrent()

            awaitItem().shouldBeProgressWith() // initial emit

            // api response
            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            assert(fakeSocket.isConnectionOpen)

            fakeNetworkConnectivityService.mockNetworkStatus(NetworkStatus.Disconnected)
            runCurrent()

            assert(!fakeSocket.isConnectionOpen)

            fakeLoader.loaderResponse = ProjectResponse(FAKE_PROJECT_DTO_2, Instant.MIN)
            fakeNetworkConnectivityService.mockNetworkStatus(NetworkStatus.Connected)
            runCurrent()

            // cached
            awaitItem().shouldBeProgressWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )

            // api response
            awaitItem().shouldBeSuccessWithData(listOf(FAKE_EXPOSED_SNIPPET_ONE, FAKE_EXPOSED_SNIPPET_TWO))
            assert(fakeSocket.isConnectionOpen)
        }
    }

    @Test
    fun `Loading snippets with project and organization id and collect as function`() = fakeScope.runTest {
        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets().test {
            assert(awaitItem() == null) // initial progress

            assert(
                awaitItem() == listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            ) // success after api call
        }
    }

    @Test
    fun `Load content snippets from cache, fetch from api and collect as function`() = fakeScope.runTest {
        fakeCache.save(TWSManagerImpl.CACHED_SNIPPETS, listOf(FAKE_SNIPPET_ONE))

        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.snippets().test {
            assert(awaitItem() == null) // initial

            val progress = awaitItem() // progress with cached items
            assert(progress == listOf(FAKE_EXPOSED_SNIPPET_ONE))

            val success = awaitItem() // success with network items
            assert(
                success == listOf(
                    FAKE_EXPOSED_SNIPPET_ONE,
                    FAKE_EXPOSED_SNIPPET_TWO,
                    FAKE_EXPOSED_SNIPPET_FOUR,
                    FAKE_EXPOSED_SNIPPET_FIVE
                )
            )
        }
    }

    private fun copyTWSManagerImpl(
        context: Context? = null,
        tag: String? = null,
        configuration: TWSConfiguration? = null,
        loader: SnippetLoadingManager? = null,
        scope: CoroutineScope? = null,
        twsSocket: TWSSocket? = null,
        localSnippetHandler: LocalSnippetHandler? = null,
        cacheManager: CacheManager? = null,
        networkConnectivityService: NetworkConnectivityService? = null,
    ): TWSManagerImpl {
        return TWSManagerImpl(
            context = context ?: mock(),
            tag = tag ?: "TestManager",
            configuration = configuration ?: TWSConfiguration.Basic("organization", "project", "apiKey"),
            loader = loader ?: fakeLoader,
            scope = scope ?: fakeScope.backgroundScope,
            twsSocket = twsSocket ?: fakeSocket,
            localSnippetHandler = localSnippetHandler ?: fakeHandler,
            cacheManager = cacheManager ?: fakeCache,
            networkConnectivityService = networkConnectivityService ?: fakeNetworkConnectivityService
        )
    }
}
