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

package si.inova.tws.manager.local_handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.time.AndroidTimeProvider
import si.inova.kotlinova.core.time.DefaultAndroidTimeProvider
import si.inova.tws.manager.data.ActionBody
import si.inova.tws.manager.data.ActionType
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.data.WebSnippetDto
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LocalSnippetHandlerImpl(
    private val scope: CoroutineScope,
    private val timeProvider: AndroidTimeProvider = DefaultAndroidTimeProvider
) : LocalSnippetHandler {
    override val updateActionFlow: MutableSharedFlow<SnippetUpdateAction> = MutableSharedFlow(replay = 0, extraBufferCapacity = 1)

    private var snippets: List<WebSnippetDto> = emptyList()
    private var scheduledJob: Job? = null  // Reference to the currently scheduled job

    private var dateDifference: Long? = null

    override fun calculateDateDifference(headerDate: String?, headerDatePattern: String) {
        val formatter = DateTimeFormatter.ofPattern(headerDatePattern)
        val zonedDateTime = ZonedDateTime.parse(headerDate, formatter)
        val instant = zonedDateTime.toInstant()

        dateDifference = timeProvider.currentInstant().minusMillis(instant.toEpochMilli()).toEpochMilli()
    }

    override suspend fun updateAndScheduleCheck(snippets: List<WebSnippetDto>) {
        // Cancel any previously scheduled check to avoid duplicate checks
        scheduledJob?.cancel()

        this.snippets = snippets
        val now = timeProvider.currentInstant().minusMillis(dateDifference ?: 0)

        // Delete all snippets that should be hidden
        checkAndDeleteSnippets(snippets, now)

        // Schedule next check
        scheduleNextDeletion()
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

    private suspend fun checkAndDeleteSnippets(snippets: List<WebSnippetDto>, now: Instant) {
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
