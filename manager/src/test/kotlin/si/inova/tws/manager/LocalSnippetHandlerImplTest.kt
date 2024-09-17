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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.time.FakeAndroidTimeProvider
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.local_handler.LocalSnippetHandlerImpl
import si.inova.tws.manager.utils.FAKE_SNIPPET_ONE
import si.inova.tws.manager.utils.FAKE_SNIPPET_THREE
import si.inova.tws.manager.utils.FAKE_SNIPPET_TWO
import si.inova.tws.manager.utils.setVisibility
import java.time.Instant
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class LocalSnippetHandlerImplTest {
    private val scope = TestScopeWithDispatcherProvider()
    private val timeProvider = FakeAndroidTimeProvider(
        currentMilliseconds = { 952077600000 + timePassedBy } // 3.3.2000 10:00
    )
    private var timePassedBy: Long = 0

    private lateinit var handler: LocalSnippetHandlerImpl

    @Before
    fun setUp() {
        handler = LocalSnippetHandlerImpl(scope, timeProvider)
        timePassedBy = 0
    }

    @Test
    fun `Schedule snippet deletion with expired snippets`() = scope.runTest {
        handler.updateActionFlow.test {
            handler.updateAndScheduleCheck(listOf(FAKE_SNIPPET_ONE.setVisibility(952077540000))) // 3.3.2020 9:59
            runCurrent()

            val action = expectMostRecentItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = FAKE_SNIPPET_ONE.id)))
        }
    }

    @Test
    fun `Check for deletion with snippets without visibility`() = scope.runTest {
        handler.updateActionFlow.test {
            handler.updateAndScheduleCheck(listOf(FAKE_SNIPPET_ONE, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()
        }
    }

    @Test
    fun `Check for deletion with active snippets and schedule its deletion`() = scope.runTest {
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(952077660000) // 3.3.2000 10:01

        handler.updateActionFlow.test {
            handler.updateAndScheduleCheck(listOf(willExpireSnippet, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()

            val delay = TimeUnit.MINUTES.toMillis(1) + 1
            timePassedBy += delay
            advanceTimeBy(TimeUnit.MINUTES.toMillis(1) + 1)

            val action = awaitItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = willExpireSnippet.id)))
        }
    }

    @Test
    fun `Check for deletion with server time in past`() = scope.runTest {
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(952077660000) // 3.3.2000 10:01

        val serverDate = Instant.ofEpochMilli(952077540000) // 3.3.2000 09:59 - 1 minute in past of current time on mobile

        handler.updateActionFlow.test {
            handler.calculateDateOffsetAndRerun(serverDate, listOf(willExpireSnippet, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()

            val delay = TimeUnit.MINUTES.toMillis(2) + 1
            timePassedBy += delay
            advanceTimeBy(delay)

            val action = awaitItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = willExpireSnippet.id)))
        }
    }

    @Test
    fun `Check for deletion with server time in future`() = scope.runTest {
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(952078260000) // 3.3.2000 10:11

        val serverDate = Instant.ofEpochMilli(952077900000) // 3.3.2000 10:05 - 5 minute in future of current time on mobile

        handler.updateActionFlow.test {
            handler.calculateDateOffsetAndRerun(serverDate, listOf(willExpireSnippet, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()

            val delay = TimeUnit.MINUTES.toMillis(6) + 1
            timePassedBy += delay
            advanceTimeBy(delay)

            val action = awaitItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = willExpireSnippet.id)))
        }
    }

    @Test
    fun `Check for deletion with server time in future and at the start empty list of snippets`() = scope.runTest {
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(952078260000) // 3.3.2000 10:11

        val serverDate = Instant.ofEpochMilli(952077900000) // 3.3.2000 10:05 - 5 minute in future of current time on mobile
        handler.calculateDateOffsetAndRerun(serverDate, emptyList())

        handler.updateActionFlow.test {
            handler.updateAndScheduleCheck(listOf(willExpireSnippet, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()

            val delay = TimeUnit.MINUTES.toMillis(6) + 1
            timePassedBy += delay
            advanceTimeBy(delay)

            val action = awaitItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = willExpireSnippet.id)))
        }
    }

    @Test
    fun `Check for deletion with active snippets and schedule its deletion after 2 checks`() = scope.runTest {
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(952077660000) // 3.3.2020 10:01

        handler.updateActionFlow.test {
            handler.updateAndScheduleCheck(listOf(willExpireSnippet, FAKE_SNIPPET_TWO, FAKE_SNIPPET_THREE))
            runCurrent()

            expectNoEvents()

            val delayMs = TimeUnit.SECONDS.toMillis(30)
            timePassedBy += delayMs
            advanceTimeBy(delayMs)

            expectNoEvents()

            timePassedBy += delayMs + 1
            advanceTimeBy(delayMs + 1)

            val action = awaitItem()
            assert(action == SnippetUpdateAction(ActionType.DELETED, ActionBody(id = willExpireSnippet.id)))
        }
    }
}

