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
package com.thewebsnippet.manager.fakes

import com.thewebsnippet.manager.time.AndroidTimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class FakeAndroidTimeProvider(
    private val currentLocalDate: () -> LocalDate = { LocalDate.MIN },
    private val currentLocalTime: () -> LocalTime = { LocalTime.MIN },
    private val currentLocalDateTime: () -> LocalDateTime = { LocalDateTime.of(currentLocalDate(), currentLocalTime()) },
    private val currentTimezone: () -> ZoneId = { ZoneId.of("UTC") },
    private val currentZonedDateTime: () -> ZonedDateTime = { ZonedDateTime.of(currentLocalDateTime(), currentTimezone()) },
    private val currentMilliseconds: () -> Long = { 0 }
) : AndroidTimeProvider {
    override fun elapsedRealtime(): Long {
        return currentMilliseconds()
    }

    override fun currentMonotonicTimeMillis(): Long {
        return currentMilliseconds()
    }

    override fun elapsedRealtimeNanos(): Long {
        return currentMilliseconds()
    }

    override fun uptimeMillis(): Long {
        return currentMilliseconds()
    }

    override fun currentTimeMillis(): Long {
        return currentMilliseconds()
    }

    override fun currentLocalDate(): LocalDate {
        return currentLocalDate.invoke()
    }

    override fun currentLocalDateTime(): LocalDateTime {
        return currentLocalDateTime.invoke()
    }

    override fun currentLocalTime(): LocalTime {
        return currentLocalTime.invoke()
    }

    override fun currentInstant(): Instant {
        return Instant.ofEpochMilli(currentTimeMillis())
    }

    override fun currentZonedDateTime(): ZonedDateTime {
        return currentZonedDateTime.invoke()
    }

    override fun systemDefaultZoneId(): ZoneId {
        return currentTimezone()
    }
}
