/*
 * Copyright 2025 INOVA IT d.o.o.
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
package com.thewebsnippet.manager.data.datasource

import com.thewebsnippet.manager.domain.model.SnippetUpdateAction
import com.thewebsnippet.manager.domain.model.TWSSnippetDto
import com.thewebsnippet.manager.domain.datasource.LocalSnippetHandler
import com.thewebsnippet.manager.domain.time.AndroidTimeProvider
import com.thewebsnippet.manager.data.time.DefaultAndroidTimeProvider
import com.thewebsnippet.manager.domain.model.ActionBody
import com.thewebsnippet.manager.domain.model.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

internal class LocalSnippetHandlerImpl(
    private val scope: CoroutineScope,
    private val timeProvider: AndroidTimeProvider = DefaultAndroidTimeProvider
) : LocalSnippetHandler {
    override val updateActionFlow: MutableSharedFlow<SnippetUpdateAction> = MutableSharedFlow(replay = 0, extraBufferCapacity = 1)

    private var snippets: List<TWSSnippetDto> = emptyList()
    private var scheduledJob: Job? = null // Reference to the currently scheduled job

    private var dateDifference: Long? = null

    override suspend fun calculateDateOffsetAndRerun(serverDate: Instant?, snippets: List<TWSSnippetDto>) {
        dateDifference = timeProvider.currentInstant().minusMillis(serverDate?.toEpochMilli() ?: 0).toEpochMilli()
        updateAndScheduleCheck(snippets)
    }

    override suspend fun updateAndScheduleCheck(snippets: List<TWSSnippetDto>) {
        // Cancel any previously scheduled check to avoid duplicate checks
        scheduledJob?.cancel()

        this.snippets = snippets
        val now = timeProvider.currentInstant().minusMillis(dateDifference ?: 0)

        // Delete all snippets that should be hidden
        checkAndDeleteSnippets(snippets, now)

        // Schedule next check
        scheduleNextDeletion()
    }

    override fun release() {
        scheduledJob?.cancel()
    }

    private fun scheduleNextDeletion() {
        val now = timeProvider.currentInstant().minusMillis(dateDifference ?: 0)

        // Schedule next deletion at earliest untilUtc of remaining snippets
        val nextScheduledCheck = snippets
            .asSequence()
            .mapNotNull { it.visibility?.untilUtc?.takeIf { hideAfter -> now.isBefore(hideAfter) } }
            .minOrNull()

        if (nextScheduledCheck == null) {
            // Do not schedule check if there are no snippets, which should be hidden after certain date
            return
        }

        // Schedule a coroutine to run after the delayMillis
        scheduledJob = scope.launch {
            delay(Duration.between(now, nextScheduledCheck).toMillis().coerceAtLeast(0L))
            updateAndScheduleCheck(snippets)
        }
    }

    private suspend fun checkAndDeleteSnippets(snippets: List<TWSSnippetDto>, now: Instant) {
        // emit all delete events for snippets that should already be hidden
        snippets.forEach { snippet ->
            snippet.visibility?.untilUtc?.let { hideAfter ->
                if (now.isAfter(hideAfter)) {
                    updateActionFlow.emit(SnippetUpdateAction(ActionType.DELETED, ActionBody(id = snippet.id)))
                }
            }
        }
    }
}
