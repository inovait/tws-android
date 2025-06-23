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

import com.thewebsnippet.data.TWSSnippet
import kotlinx.coroutines.flow.Flow

/**
 * A manager interface for handling snippets, their properties, and lifecycle events.
 *
 * Instances of [TWSManager] should be created using [TWSFactory].
 *
 * ### Example Usage
 * ```kotlin
 * val manager = TWSFactory.get(context)
 * val projectSnippets = manager.snippets.collectAsStateWithLifecycle(null).value
 * // Use projectSnippets with TWSView or process them as needed
 * ```
 *
 */
interface TWSManager {
    /**
     * A flow that combines remote snippets with local properties to keep the data in sync and up-to-date.
     * Use `collectAsStateWithLifecycle` to automatically start and stop data collection based on the lifecycle.
     */
    val snippets: Flow<TWSOutcome<List<TWSSnippet>>>

    /**
     * Retrieves the list of snippets as a flow of data only.
     * Use `collectAsStateWithLifecycle` to automatically start and stop data collection based on the lifecycle.
     *
     * @return A [Flow] emitting the current list of snippets, cached, remote or `null` if unavailable.
     */
    fun snippets(): Flow<List<TWSSnippet>?>

    /**
     * Forces a refresh of the snippets by reloading them from the remote source.
     * Updates are emitted in [snippets] flow to all active collectors and cached for future use.
     */
    fun forceRefresh()

    /**
     * Creates a connection to a backend service and prepares snippets, note that this MUST be called before manager can
     * actually be used, otherwise connection to a backend service will not be established.
     */
    fun register()

    /**
     * Updates or adds local properties for a specific snippet.
     * These properties are  applied to the snippet for all active collectors.
     *
     * @param id The unique identifier of the snippet.
     * @param localProps A map of properties to associate with the snippet.
     */
    fun set(id: String, localProps: Map<String, Any>)

    fun logEvent(event: String)
}
