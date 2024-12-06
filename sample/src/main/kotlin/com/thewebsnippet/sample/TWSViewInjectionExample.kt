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

import androidx.compose.runtime.Composable
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
 * Demonstrates how to dynamically inject external resources into a `TWSView` using the `dynamicResources` property.
 *
 * The `dynamicResources` field in the `TWSSnippet` allows you to inject external resources (CSS, JavaScript) to your
 * web page to enhance the behavior or add styling of the rendered content. This provides a powerful mechanism
 * to customize the appearance or functionality of the content loaded in `TWSView`.
 *
 * In this example, we add a CSS and Javascript files to original HTML. The attached resources are specified with a URL and a
 * content type of `CSS`. All resources are already prepared and added to the snippet on the platform (can be found on snippet
 * details page, under the dynamic resources), so no specific code is required to enable this feature.
 *
 * A working example can be found [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewInjectionExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 */
@Composable
fun TWSViewInjectionExample(
    twsInjectionViewModel: TWSInjectionViewModel = hiltViewModel()
) {
    // Collect snippets for your project
    val content = twsInjectionViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value

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
class TWSInjectionViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    // Collecting TWSManager.snippets, which returns the current state, which
    // exposes TWSOutcome.Error, TWSOutcome.Progress or TWSOutcome.Success state.
    val twsSnippetsFlow: Flow<TWSOutcome<List<TWSSnippet>>> = manager.snippets
        .map { data ->
            data.mapData { it.filter { snippet -> snippet.props["page"] == injectionPage } }
        }

    private val injectionPage = "injection"
}
