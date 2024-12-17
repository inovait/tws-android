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
package com.thewebsnippet.manager.localhandler

import com.thewebsnippet.manager.data.ActionBody
import com.thewebsnippet.manager.data.ActionType
import com.thewebsnippet.manager.data.SnippetUpdateAction
import com.thewebsnippet.manager.data.TWSSnippetDto
import com.thewebsnippet.manager.time.AndroidTimeProvider
import com.thewebsnippet.manager.time.DefaultAndroidTimeProvider
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
