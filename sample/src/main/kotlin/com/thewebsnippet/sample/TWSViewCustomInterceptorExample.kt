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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.TWSManager
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.sample.ui.Screen
import com.thewebsnippet.view.TWSView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Demonstrates how to use a custom `TWSViewInterceptor` with `TWSView` to intercept URL loading.
 * By implementing the `interceptUrlCallback`, you can override the default web page loading behavior
 * and open your native application flow (e.g., navigate to a specific screen in your app).
 *
 * This is particularly useful when your web content includes links that should trigger in-app navigation
 * instead of rendering a web page.
 *
 * Working example can be found [here](https://github.com/inovait/tws-android-sdk/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewCustomInterceptorExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample com.thewebsnippet.sample.TWSViewCustomInterceptorExample
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

/** @suppress: viewmodel should not be documented */
@HiltViewModel
class TWSInterceptorViewModel @Inject constructor(
    manager: TWSManager
) : ViewModel() {
    val twsSnippetsFlow: Flow<List<TWSSnippet>?> = manager.snippets()
}
