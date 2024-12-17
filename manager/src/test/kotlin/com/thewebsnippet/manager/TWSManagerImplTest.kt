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

import android.content.Context
import app.cash.turbine.test
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.manager.cache.CacheManager
import com.thewebsnippet.manager.data.ActionBody
import com.thewebsnippet.manager.data.ActionType
import com.thewebsnippet.manager.data.NetworkStatus
import com.thewebsnippet.manager.data.ProjectDto
import com.thewebsnippet.manager.data.SnippetUpdateAction
import com.thewebsnippet.manager.fakes.FakeCacheManager
import com.thewebsnippet.manager.fakes.FakeLocalSnippetHandler
import com.thewebsnippet.manager.fakes.FakeNetworkConnectivityService
import com.thewebsnippet.manager.fakes.FakeTWSSocket
import com.thewebsnippet.manager.fakes.manager.FakeSnippetLoadingManager
import com.thewebsnippet.manager.localhandler.LocalSnippetHandler
import com.thewebsnippet.manager.manager.snippet.ProjectResponse
import com.thewebsnippet.manager.manager.snippet.SnippetLoadingManager
import com.thewebsnippet.manager.service.NetworkConnectivityService
import com.thewebsnippet.manager.utils.FAKE_EXPOSED_SNIPPET_FIVE
import com.thewebsnippet.manager.utils.FAKE_EXPOSED_SNIPPET_FOUR
import com.thewebsnippet.manager.utils.FAKE_EXPOSED_SNIPPET_ONE
import com.thewebsnippet.manager.utils.FAKE_EXPOSED_SNIPPET_THREE
import com.thewebsnippet.manager.utils.FAKE_EXPOSED_SNIPPET_TWO
import com.thewebsnippet.manager.utils.FAKE_PROJECT_DTO
import com.thewebsnippet.manager.utils.FAKE_PROJECT_DTO_2
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_FIVE
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_FOUR
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_ONE
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_THREE
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_TWO
import com.thewebsnippet.manager.utils.shouldBeProgressWith
import com.thewebsnippet.manager.utils.shouldBeProgressWithData
import com.thewebsnippet.manager.utils.shouldBeSuccessWithData
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import com.thewebsnippet.manager.utils.toActionBody
import com.thewebsnippet.manager.websocket.TWSSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TWSManagerImplTest {
    private val fakeScope = testScopeWithDispatcherProvider()

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
        webSnippetManager = copyTWSManagerImpl(configuration = TWSConfiguration.Shared("shared"))

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
                    FAKE_SNIPPET_ONE.toActionBody().copy(target = "www.example.com")
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
                    FAKE_SNIPPET_ONE.toActionBody().copy(target = "www.updated.com")
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

            fakeHandler.mockUpdateAction(SnippetUpdateAction(ActionType.UPDATED, FAKE_SNIPPET_ONE.toActionBody()))
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
                    data = FAKE_SNIPPET_ONE.toActionBody().copy(
                        dynamicResources = listOf(TWSAttachment("https://test.cs", TWSAttachmentType.CSS))
                    )
                )
            )
            runCurrent()

            awaitItem().shouldBeSuccessWithData(
                listOf(
                    FAKE_EXPOSED_SNIPPET_ONE.copy(
                        dynamicResources = listOf(TWSAttachment("https://test.cs", TWSAttachmentType.CSS))
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

            webSnippetManager.set(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))

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

            webSnippetManager.set(
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

            webSnippetManager.set(FAKE_SNIPPET_ONE.id, mapOf("name" to "Chris"))
            webSnippetManager.set(FAKE_SNIPPET_ONE.id, mapOf("surname" to "Donovan"))

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

        webSnippetManager = copyTWSManagerImpl(configuration = TWSConfiguration.Shared("shared"))

        fakeLoader.loaderResponse = ProjectResponse(
            FAKE_PROJECT_DTO,
            Instant.MIN
        )

        webSnippetManager.forceRefresh()

        webSnippetManager.snippets.test {
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
            configuration = configuration ?: TWSConfiguration.Basic("organization", "project"),
            loader = loader ?: fakeLoader,
            scope = scope ?: fakeScope.backgroundScope,
            twsSocket = twsSocket ?: fakeSocket,
            localSnippetHandler = localSnippetHandler ?: fakeHandler,
            cacheManager = cacheManager ?: fakeCache,
            networkConnectivityService = networkConnectivityService ?: fakeNetworkConnectivityService
        )
    }
}
