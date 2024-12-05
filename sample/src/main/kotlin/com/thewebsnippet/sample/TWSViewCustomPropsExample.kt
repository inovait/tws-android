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

package com.thewebsnippet.sample

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
import com.thewebsnippet.core.TWSView
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.manager.TWSOutcome
import com.thewebsnippet.manager.mapData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Demonstrates how to use `TWSView` with custom properties (`props`) defined in the `TWSSnippet`.
 * The `props` field in the `TWSSnippet` allows you to attach custom metadata to a snippet in JSON format.
 * These properties can be used for various purposes, such as configuring the behavior of your app
 * or providing additional context to your view.
 *
 * In this example, we access the custom `tabName` property (if it exists) and display it using a `Text` composable.
 * The `props` can include values of any type, including classes, allowing you to extend the functionality
 * of the snippet with complex data structures.
 *
 * This flexibility allows you to pass any additional information your app might need to handle the snippet.
 *
 * Working example can be found at [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/app/src/main/kotlin/si/inova/tws/sample/examples/tabs/TWSViewCustomTabsExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample com.thewebsnippet.sample.BottomTabsRow
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
private fun TWSViewComponentWithTabs(content: ImmutableList<TWSSnippet>) {
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

/** @suppress: viewmodel should not be documented */
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
