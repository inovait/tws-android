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
package com.thewebsnippet.manager.core

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
