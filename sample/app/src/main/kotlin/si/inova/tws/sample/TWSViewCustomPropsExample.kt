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

package si.inova.tws.sample


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import si.inova.tws.core.TWSView
import si.inova.tws.data.TWSSnippet
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.manager.mapData
import si.inova.tws.sample.components.ErrorView
import si.inova.tws.sample.components.LoadingView
import javax.inject.Inject

/**
 * A composable function that renders a screen showcasing the use of custom properties defined on snippet
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

@Composable
fun TWSViewComponentWithTabs(content: ImmutableList<TWSSnippet>) {
    var currentTab by remember { mutableIntStateOf(0) }
    val onClick: (Int) -> Unit = {
        currentTab = it
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomTabsRow(content, currentTab, onClick) })
    { padding ->
        TWSView(
            snippet = content[currentTab],
            modifier = Modifier.padding(padding),
            loadingPlaceholderContent = { LoadingView() },
            errorViewContent = { ErrorView(it) }
        )
    }
}

@Composable
private fun BottomTabsRow(
    content: ImmutableList<TWSSnippet>,
    currentTab: Int,
    onClick: (Int) -> Unit
) {
    TabRow(currentTab) {
        content.forEachIndexed { index, item ->
            // Setting text and icons for each tab using custom snippet properties
            Tab(
                selected = index == currentTab,
                onClick = { onClick(index) },
                text = (item.props["tabName"] as? String)?.let { { Text(text = it, maxLines = 1) } },
                icon = (item.props["tabIcon"] as? String)?.asTabIconDrawable()?.let {
                    {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "Tab icon"
                        )
                    }
                }
            )
        }
    }
}

private fun String.asTabIconDrawable(): Int {
    return when (this) {
        "home" -> R.drawable.home
        "search" -> R.drawable.search
        "settings" -> R.drawable.settings
        "news" -> R.drawable.news
        "sports_soccer" -> R.drawable.sports_soccer
        "directions_car" -> R.drawable.directions_car
        "public" -> R.drawable.resource_public
        "map" -> R.drawable.map
        "sunny" -> R.drawable.sunny
        "person" -> R.drawable.person
        "list" -> R.drawable.list
        else -> R.drawable.broken_image
    }
}

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
