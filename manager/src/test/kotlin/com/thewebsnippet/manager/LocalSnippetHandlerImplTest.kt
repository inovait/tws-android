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
import com.thewebsnippet.manager.data.ActionBody
import com.thewebsnippet.manager.data.ActionType
import com.thewebsnippet.manager.data.SnippetUpdateAction
import com.thewebsnippet.manager.fakes.FakeAndroidTimeProvider
import com.thewebsnippet.manager.localhandler.LocalSnippetHandlerImpl
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_ONE
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_THREE
import com.thewebsnippet.manager.utils.FAKE_SNIPPET_TWO
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE_FUTURE_1
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE_FUTURE_11
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE_FUTURE_5
import com.thewebsnippet.manager.utils.MILLISECONDS_DATE_PAST
import com.thewebsnippet.manager.utils.setVisibility
import com.thewebsnippet.manager.utils.testScopeWithDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class LocalSnippetHandlerImplTest {
    private val scope = testScopeWithDispatcherProvider()
    private val timeProvider = FakeAndroidTimeProvider(
        currentMilliseconds = { MILLISECONDS_DATE + timePassedBy } // 3.3.2000 10:00
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
            handler.updateAndScheduleCheck(listOf(FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_PAST))) // 3.3.2020 9:59
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
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_FUTURE_1) // 3.3.2000 10:01

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
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_FUTURE_1) // 3.3.2000 10:01

        // 3.3.2000 09:59 - 1 minute in past of current time on mobile
        val serverDate = Instant.ofEpochMilli(MILLISECONDS_DATE_PAST)

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
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_FUTURE_11) // 3.3.2000 10:11

        // 3.3.2000 10:05 - 5 minute in future of current time on mobile
        val serverDate = Instant.ofEpochMilli(MILLISECONDS_DATE_FUTURE_5)

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
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_FUTURE_11) // 3.3.2000 10:11

        // 3.3.2000 10:05 - 5 minute in future of current time on mobile
        val serverDate = Instant.ofEpochMilli(MILLISECONDS_DATE_FUTURE_5)
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
        val willExpireSnippet = FAKE_SNIPPET_ONE.setVisibility(MILLISECONDS_DATE_FUTURE_1) // 3.3.2020 10:01

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
