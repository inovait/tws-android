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

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.mapData
import si.inova.tws.manager.snippet.TWSSnippetLoader
import si.inova.tws.manager.snippet.TWSSnippetLoaderImpl

internal class TWSManagerImpl(
    context: Context,
    private val configuration: TWSConfiguration,
    tag: String = "",
    private val snippetLoad: TWSSnippetLoader = TWSSnippetLoaderImpl(context, tag, configuration)
) : TWSManager {
    private val _localProps: MutableStateFlow<Map<String, Map<String, Any>>> = MutableStateFlow(emptyMap())

    /**
     * collect [snippetsFlow] using `collectAsStateWithLifecycle`
     * to ensure the WebSocket disconnects when not needed and reconnects appropriately.
     */
    override val snippetsFlow = combine(snippetLoad.snippetsFlow, _localProps) { snippetsOutcome, localProps ->
        snippetsOutcome.mapData { snippets ->
            snippets.map {
                it.copy(props = it.props + (localProps[it.id].orEmpty()))
            }
        }
    }.onStart {
        forceRefresh()
    }

    override val mainSnippetIdFlow: Flow<String?> = snippetLoad.mainSnippetIdFlow

    override suspend fun forceRefresh() {
        snippetLoad.forceRefresh()
    }

    override fun setLocalProps(id: String, localProps: Map<String, Any>) {
        val currentLocalProps = _localProps.value.toMutableMap()
        currentLocalProps[id] = localProps
        _localProps.update { currentLocalProps }
    }
}
