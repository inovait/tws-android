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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import si.inova.tws.core.TWSView
import si.inova.tws.core.data.TWSViewInterceptor
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.sample.components.LoadingSpinner
import si.inova.tws.sample.components.OnErrorComponent

@Composable
fun NavigationScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val manager = TWSFactory.get(
        context,
        TWSConfiguration.Basic(organizationId, projectId, "apiKey")
    )

    NavigationContent(navController, manager)
}

@Composable
private fun NavigationContent(
    navController: NavController,
    manager: TWSManager
) {
    val content = manager.snippets.collectAsStateWithLifecycle(null).value

    content?.let {
        when {
            !content.data.isNullOrEmpty() -> {
                val data = content.data ?: return
                val navigationScreen = data[0]
                TWSView(
                    snippet = navigationScreen,
                    // Set custom loading placeholder
                    loadingPlaceholderContent = { LoadingSpinner() },
                    // set custom error placeholder
                    errorViewContent = { OnErrorComponent() },
                    // Handling urls natively
                    interceptUrlCallback = TWSViewInterceptor { uri ->
                        with(uri.toString()) {
                            when {
                                contains("/customTabsExample") -> {
                                    navController.navigate(Screen.TWSViewCustomTabsExample.route)
                                    return@TWSViewInterceptor true
                                }

                                contains("/mustacheExample") -> {
                                    navController.navigate(Screen.TWSViewMustacheExample.route)
                                    return@TWSViewInterceptor true
                                }

                                contains("/injectionExample") -> {
                                    navController.navigate(Screen.TWSViewInjectionExample.route)
                                    return@TWSViewInterceptor true
                                }

                                contains("/loginRedirectExample") -> {
                                    navController.navigate(Screen.TWSViewLoginRedirectExample.route)
                                    return@TWSViewInterceptor true
                                }

                                else -> return@TWSViewInterceptor false
                            }
                        }
                    }
                )
            }

            content is TWSOutcome.Error -> {
                OnErrorComponent()
            }

            content is TWSOutcome.Progress -> {
                LoadingSpinner()
            }
        }
    }
}

private const val organizationId = "examples"
private const val projectId = "example5"
