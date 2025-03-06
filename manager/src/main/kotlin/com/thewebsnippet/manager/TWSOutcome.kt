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

/**
 * A sealed class representing the outcome of an operation, typically for API calls.
 * It encapsulates three possible states:
 *
 * - [Progress]: Indicates that data is being fetched from the API. If available, it contains cached data.
 * - [Success]: Indicates that the API call was successful, and the data is up-to-date.
 * - [Error]: Indicates that the API call failed. It provides the exception details and, if available, the last known data.
 */
sealed class TWSOutcome<out T> {
    /**
     * The data associated with the current state.
     * - In [Progress], this is cached data, if cache is available.
     * - In [Success], this is the up-to-date result from the API.
     * - In [Error], this is the last available data, if any.
     */
    abstract val data: T?

    /**
     * Represents the progress state when fetching data from the API.
     * @param data Cached data, if available, during the fetch process.
     */
    data class Progress<out T>(override val data: T? = null) : TWSOutcome<T>()

    /**
     * Represents a successful API response.
     * @param data The up-to-date data returned by the API.
     */
    data class Success<out T>(override val data: T) : TWSOutcome<T>()

    /**
     * Represents an error state when the API call fails.
     * @param exception The exception that occurred during the API call.
     * @param data The last available data, if any, before the failure.
     */
    data class Error<out T>(val exception: Exception, override val data: T? = null) : TWSOutcome<T>()
}

/**
 * Map data of this outcome, while keeping the type.
 *
 * If provided Outcome has no data, [mapper] never gets called.
 */
fun <A, B> TWSOutcome<A>.mapData(mapper: (A) -> B): TWSOutcome<B> {
    return when (this) {
        is TWSOutcome.Error -> TWSOutcome.Error(exception, data?.let { mapper(it) })
        is TWSOutcome.Progress -> TWSOutcome.Progress(data?.let { mapper(it) })
        is TWSOutcome.Success -> TWSOutcome.Success(mapper(data))
    }
}
