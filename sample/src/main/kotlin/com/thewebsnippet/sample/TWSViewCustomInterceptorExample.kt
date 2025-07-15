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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.manager.core.TWSManager
import com.thewebsnippet.manager.core.TWSOutcome
import com.thewebsnippet.manager.core.mapData
import com.thewebsnippet.sample.components.ErrorView
import com.thewebsnippet.sample.components.LoadingView
import com.thewebsnippet.sample.components.sampleErrorView
import com.thewebsnippet.sample.ui.Screen
import com.thewebsnippet.view.TWSView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Demonstrates how to use a custom `TWSViewInterceptor` with `TWSView` to intercept URL loading.
 * By implementing the `interceptUrlCallback`, you can override the default web page loading behavior
 * and open your native application flow (e.g., navigate to a specific screen in your app).
 *
 * This is particularly useful when your web content includes links that should trigger in-app navigation
 * instead of rendering a web page.
 *
 * Working example can be found [here](https://github.com/inovait/tws-android/blob/develop/sample/src/main/kotlin/com/thewebsnippet/sample/TWSViewCustomInterceptorExample.kt).
 * Download the Sample app from our web page to explore this functionality interactively.
 *
 * @sample com.thewebsnippet.sample.TWSViewCustomInterceptorExample
 */
@Composable
fun TWSViewCustomInterceptorExample(
    navController: NavController,
    twsInterceptorViewModel: TWSInterceptorViewModel = hiltViewModel()
) {
    val homeSnippet = twsInterceptorViewModel.twsSnippetsFlow.collectAsStateWithLifecycle(TWSOutcome.Progress()).value

    when {
        homeSnippet.data != null -> {
            val data = homeSnippet.data ?: return
            TWSView(
                snippet = data,
                loadingPlaceholderContent = { LoadingView() },
                errorViewContent = sampleErrorView(),
                // Handling urls natively
                interceptUrlCallback = { url ->
                    val urlString = url.toString()
                    val route = when {
                        urlString.contains("/customTabsExample") -> Screen.TWSViewCustomTabsExampleKey.route
                        urlString.contains("/mustacheExample") -> Screen.TWSViewMustacheExampleKey.route
                        urlString.contains("/injectionExample") -> Screen.TWSViewInjectionExampleKey.route
                        urlString.contains("/permissionsExample") -> Screen.TWSViewPermissionsExampleKey.route
                        urlString.contains("/userEngagementExample") -> Screen.NativeViewUserEngagementExampleKey.route
                        else -> null
                    }

                    route?.let { navController.navigate(it) }
                    route != null
                }
            )
        }

        homeSnippet is TWSOutcome.Error -> {
            ErrorView(
                errorText = stringResource(R.string.error_message),
                modifier = Modifier.fillMaxSize(),
                callback = twsInterceptorViewModel::forceRefresh
            )
        }

        homeSnippet is TWSOutcome.Progress -> {
            LoadingView(modifier = Modifier.fillMaxSize())
        }

        homeSnippet.data == null -> {
            ErrorView(
                errorText = stringResource(R.string.snippet_not_found),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** @suppress: viewmodel should not be documented */
@HiltViewModel
class TWSInterceptorViewModel @Inject constructor(
    private val manager: TWSManager
) : ViewModel() {
    val twsSnippetsFlow: Flow<TWSOutcome<TWSSnippet?>> = manager.snippets.map { data ->
        data.mapData { snippets ->
            snippets.firstOrNull {
                it.id == "customInterceptors"
            }
        }
    }

    fun forceRefresh() {
        manager.forceRefresh()
    }
}
