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
package com.thewebsnippet.manager.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal interface TimeProvider {
    /**
     * @return difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     *
     * @see System.currentTimeMillis
     */
    fun currentTimeMillis(): Long

    /**
     * The value returned represents milliseconds since some fixed but arbitrary origin time
     * (perhaps in the future, so values may be negative).
     *
     * This is not related to any other notion of system or wall-clock time,
     * meaning the value will not change if user changes system time settings. That's why
     * it is recommended to use this to measure elapsed time instead of [currentTimeMillis].
     *
     * This value should not be stored persistently between reboots / process instances. It should be kept in-memory only.
     */
    fun currentMonotonicTimeMillis(): Long

    fun currentLocalDate(): LocalDate

    fun currentLocalDateTime(): LocalDateTime

    fun currentLocalTime(): LocalTime

    fun currentInstant(): Instant

    fun currentZonedDateTime(): ZonedDateTime

    fun systemDefaultZoneId(): ZoneId
}
