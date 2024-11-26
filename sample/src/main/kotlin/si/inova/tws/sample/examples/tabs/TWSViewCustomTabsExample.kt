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

package si.inova.tws.sample.examples.tabs


import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import si.inova.tws.data.TWSSnippet
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.manager.mapData
import si.inova.tws.sample.R
import si.inova.tws.sample.components.ErrorView
import si.inova.tws.sample.components.LoadingView
import javax.inject.Inject

/**
 * @param twsCustomTabsViewModel A viewModel that provides access to the [TWSOutcome].
 */
@Composable
fun TWSViewCustomTabsExample(
    twsCustomTabsViewModel: TWSCustomTabsViewModel = hiltViewModel()
) {
    // Collect snippets for your project
    val content = twsCustomTabsViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value

    content?.let {
        when {
            !content.data.isNullOrEmpty() -> {
                val data = content.data ?: return
                TWSViewComponentWithTabs(data.toImmutableList())
            }

            content is TWSOutcome.Error -> {
                ErrorView(content.exception.message ?: stringResource(R.string.error_message))
            }

            content is TWSOutcome.Progress -> {
                LoadingView()
            }
        }
    }
}

/**
 * @constructor Global instance of [TWSManager].
 * @property twsSnippetsFlow A Flow collecting [TWSOutcome] state from the manager,
 * filtered by a custom property "page" and sorted by a custom property "tabSortKey".
 */
@HiltViewModel
class TWSCustomTabsViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    // Collecting TWSManager.snippets, which returns the current state, which
    // exposes TWSOutcome.Error, TWSOutcome.Progress or TWSOutcome.Success state.
    val twsSnippetsFlow: Flow<TWSOutcome<List<TWSSnippet>>> = manager.snippets.map { data ->
        data.mapData { it.filter { snippet -> snippet.props["page"] == propsPage } }
    }.map { data ->
        // Sort tabs with custom tabSortKey property
        data.mapData { it.sortedBy { snippet -> snippet.props["tabSortKey"] as? String } }
    }

    private val propsPage = "snippetProps"
}
