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
     * actually be user, otherwise connection to a backend service will not be established.
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
}
