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

package com.thewebsnippet.manager.data.manager

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.core.TWSManager
import com.thewebsnippet.manager.core.TWSOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoOpManager : TWSManager {
    override val snippets: Flow<TWSOutcome<List<TWSSnippet>>>
        get() = flowOf(TWSOutcome.Success(emptyList()))

    override fun snippets(): Flow<List<TWSSnippet>?> {
        return flowOf(emptyList())
    }

    override fun forceRefresh() {
        // No operation
    }

    override fun register() {
        // No operation
    }

    override fun set(id: String, localProps: Map<String, Any>) {
        // No operation
    }
}
