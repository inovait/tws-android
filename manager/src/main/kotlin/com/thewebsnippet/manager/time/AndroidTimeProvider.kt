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

import android.os.SystemClock

internal interface AndroidTimeProvider : TimeProvider {
    /**
     * @return number of milliseconds since the system was booted, including deep sleep
     *
     * @see [SystemClock.elapsedRealtime]
     */
    fun elapsedRealtime(): Long

    /**
     * @return number of nanoseconds since the system was booted, including deep sleep
     *
     * @see [SystemClock.elapsedRealtimeNanos]
     */
    fun elapsedRealtimeNanos(): Long

    /**
     * @return number of nanoseconds since the system was booted, excluding deep sleep
     *
     * @see [SystemClock.uptimeMillis]
     */
    fun uptimeMillis(): Long
}
