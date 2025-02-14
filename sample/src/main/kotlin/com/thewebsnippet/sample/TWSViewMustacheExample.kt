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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.manager.TWSOutcome
import com.thewebsnippet.manager.mapData
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.sample.components.TWSViewComponentWithPager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Demonstrates how to use the `Mustache` engine with `TWSView` in combination with local properties on the snippet.
 *
 * If the `TWSSnippet` has its `engine` set to `TWSEngine.Mustache`, all properties defined in the `props`
 * are used for Mustache template processing. This allows you to dynamically render content based on the provided data.
 *
 * In this example, we showcase how to add additional local properties to the snippet using the manager.
 * All new properties are added to the existing `props` (which are defined on the snippet details page on
 * our platform), demonstrating how to extend the snippet's properties dynamically at runtime.
 *
 * You can see a working example [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewMustacheExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 *  Hint: try changing the props on howToMustache snippet and observe changes in the app instantly.
 *
 * @sample com.thewebsnippet.sample.TWSViewMustacheExample
 */
@Composable
fun TWSViewMustacheExample(
    twsMustacheViewModel: TWSMustacheViewModel = hiltViewModel()
) {
    // Set local properties
    LaunchedEffect(Unit) {
        val localProps =
            mapOf(
                "welcome_email" to
                    mapOf(
                        "name" to "Alice",
                        "company" to "TheWebSnippet",
                        "guide_url" to "https://mustache.github.io",
                        "community_name" to "TWS dev team",
                        "support_email" to "support@TWS.com"
                    )
            )
        twsMustacheViewModel.setProps("howToMustache", localProps)
    }

    // Collect snippets for your project
    val content = twsMustacheViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value

    content?.let {
        when {
            !content.data.isNullOrEmpty() -> {
                val data = content.data ?: return
                TWSViewComponentWithPager(data.toImmutableList())
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

/** @suppress: viewmodel should not be documented */
@HiltViewModel
class TWSMustacheViewModel @Inject constructor(
    private val manager: TWSManager
) : ViewModel() {
    // Collecting TWSManager.snippets, which returns the current state, which
    // exposes TWSOutcome.Error, TWSOutcome.Progress or TWSOutcome.Success state.
    val twsSnippetsFlow: Flow<TWSOutcome<List<TWSSnippet>>> = manager.snippets.map { data ->
        data.mapData { outcome ->
            outcome.filter { it.props["page"] == mustachePage }
                .sortedBy { it.props["tabSortKey"] as? String }
        }
    }

    /**
     * A function that exposes [TWSManager.set], for setting the local properties of a [TWSSnippet].
     *
     * @param id Unique id of the snippet.
     * @param localProps A map of properties that will get added to the [TWSSnippet].
     */
    fun setProps(id: String, localProps: Map<String, Any>) {
        manager.set(id, localProps)
    }

    private val mustachePage = "mustache"
}
