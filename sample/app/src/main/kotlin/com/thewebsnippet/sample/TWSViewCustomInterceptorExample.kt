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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.thewebsnippet.core.TWSView
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.sample.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A composable function that renders a screen used for navigation, showcasing the use of 'TWSViewInterceptor' to handle URLs natively.
 */
@Composable
fun TWSViewCustomInterceptorExample(
    navController: NavController,
    twsInterceptorViewModel: TWSInterceptorViewModel = hiltViewModel()
) {
    val homeSnippet = twsInterceptorViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(null).value
        ?.firstOrNull { it.id == "customInterceptors" } ?: return

    TWSView(
        snippet = homeSnippet,
        loadingPlaceholderContent = { LoadingView() },
        errorViewContent = { ErrorView(it) },
        // Handling urls natively
        interceptUrlCallback = { url ->
            val urlString = url.toString()
            val route = when {
                urlString.contains("/customTabsExample") -> Screen.TWSViewCustomTabsExampleKey.route
                urlString.contains("/mustacheExample") -> Screen.TWSViewMustacheExampleKey.route
                urlString.contains("/injectionExample") -> Screen.TWSViewInjectionExampleKey.route
                urlString.contains("/permissionsExample") -> Screen.TWSViewPermissionsExampleKey.route
                else -> null
            }

            route?.let { navController.navigate(it) }
            route != null
        }
    )
}

@HiltViewModel
class TWSInterceptorViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    val twsSnippetsFlow: Flow<List<TWSSnippet>?> = manager.snippets()
}
