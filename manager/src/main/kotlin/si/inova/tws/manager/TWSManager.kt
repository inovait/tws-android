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

package si.inova.tws.manager

import kotlinx.coroutines.flow.Flow
import si.inova.tws.data.TWSSnippet

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
 * ### Defining Metadata in Android Manifest
 * To use [TWSManager], you must define the required metadata in your `AndroidManifest.xml` file:
 * ```xml
 * <application>
 *     <!-- Other application elements -->
 *     <meta-data
 *         android:name="si.inova.tws.ORGANIZATION_ID"
 *         android:value="your_organization_id_here" />
 *     <meta-data
 *         android:name="si.inova.tws.PROJECT_ID"
 *         android:value="your_project_id_here" />
 * </application>
 * ```
 */
interface TWSManager {
    /**
     * A flow that combines remote snippets with local properties to keep the data in sync and up-to-date.
     * Use `collectAsStateWithLifecycle` to automatically start and stop data collection based on the lifecycle.
     */
    val snippets: Flow<TWSOutcome<List<TWSSnippet>>>

    /**
     * A flow that emits the ID of the main snippet. Available only when opening shared snippet.
     * Use `collectAsStateWithLifecycle` to automatically start and stop data collection based on the lifecycle.
     */
    val mainSnippetIdFlow: Flow<String?>

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
     * Updates or adds local properties for a specific snippet.
     * These properties are  applied to the snippet for all active collectors.
     *
     * @param id The unique identifier of the snippet.
     * @param localProps A map of properties to associate with the snippet.
     */
    fun set(id: String, localProps: Map<String, Any>)
}
