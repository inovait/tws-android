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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.core.TWSManager
import com.thewebsnippet.manager.core.TWSOutcome
import com.thewebsnippet.manager.core.mapData
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.sample.components.sampleErrorView
import com.thewebsnippet.view.TWSView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
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
 * Working example can be found [here](https://github.com/inovait/tws-android/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewCustomPropsExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * Hint: try changing tabName or tabIcon on customTabs snippet and observe changes in the app instantly.
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
            errorViewContent = sampleErrorView()
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
        data.mapData { outcome ->
            outcome.filter { it.props["page"] == propsPage }
                .sortedBy { it.props["tabSortKey"] as? String }
        }
    }

    private val propsPage = "snippetProps"
}
