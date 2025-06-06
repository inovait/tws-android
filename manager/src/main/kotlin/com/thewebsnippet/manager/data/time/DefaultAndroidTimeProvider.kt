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
package com.thewebsnippet.manager.data.time

import android.os.SystemClock
import com.thewebsnippet.manager.domain.time.AndroidTimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal object DefaultAndroidTimeProvider : AndroidTimeProvider {
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun currentMonotonicTimeMillis(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun currentLocalDate(): LocalDate {
        return LocalDate.now()
    }

    override fun currentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now()
    }

    override fun currentLocalTime(): LocalTime {
        return LocalTime.now()
    }

    override fun currentInstant(): Instant {
        return Instant.now()
    }

    override fun currentZonedDateTime(): ZonedDateTime {
        return ZonedDateTime.now()
    }

    override fun systemDefaultZoneId(): ZoneId {
        return ZoneId.systemDefault()
    }

    override fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun elapsedRealtimeNanos(): Long {
        return SystemClock.elapsedRealtimeNanos()
    }

    override fun uptimeMillis(): Long {
        return SystemClock.uptimeMillis()
    }
}
