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

/**
 * Standard wrapper for an operation. It can be either [Progress], [Success] or [Error].
 */
sealed class TWSOutcome<out T> {
    abstract val data: T?

    data class Progress<out T>(override val data: T? = null) : TWSOutcome<T>()

    data class Success<out T>(override val data: T) : TWSOutcome<T>()

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
