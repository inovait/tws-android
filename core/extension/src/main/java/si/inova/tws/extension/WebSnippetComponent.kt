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

package si.inova.tws.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import si.inova.tws.core.WebSnippetComponent
import si.inova.tws.core.data.WebViewNavigator
import si.inova.tws.core.data.WebViewState
import si.inova.tws.core.data.rememberSaveableWebViewState
import si.inova.tws.core.data.rememberWebViewNavigator
import si.inova.tws.core.util.compose.SnippetErrorView
import si.inova.tws.core.util.compose.SnippetLoadingView
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSSdk

/**
 *
 * WebSnippetComponent is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param name Filters by name within the current manager.
 * @param modifier A compose modifier.
 * @param managerTag Set to use a specific manager; defaults to global if not provided.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param webViewState State of WebView.
 * @param displayErrorViewOnError Whether to show a custom error view if loading the WebView content fails.
 * If set to true, the provided errorViewContent will be displayed in case of errors.
 * @param errorViewContent A custom composable that defines the UI content to display when there's an error
 * loading WebView content. Used only if [displayErrorViewOnError] is set to true.
 * @param displayPlaceholderWhileLoading If set to true, a placeholder or loading animation will be
 *  * shown while the WebView content is loading.
 * @param loadingPlaceholderContent A custom composable that defines the UI content to show while the WebView content is loading.
 *  Used only if [displayPlaceholderWhileLoading] is set to true.
 * @param interceptOverrideUrl A lambda function that is invoked when a URL in WebView will be loaded.
 * Returning true prevents navigation to the new URL (and allowing you to define custom behavior for specific urls),
 * while returning false allows it to proceed.
 * @param googleLoginRedirectUrl A URL to which user is redirected after successful Google login. This will allow us to redirect
 * user back to the app after login in Custom Tabs has been completed.
 * @param isRefreshable if we allow to create pull to refresh
 */
@Composable
fun WebSnippetComponent(
    name: String,
    modifier: Modifier = Modifier,
    managerTag: String? = null,
    navigator: WebViewNavigator = rememberWebViewNavigator(name),
    webViewState: WebViewState = rememberSaveableWebViewState(name),
    displayErrorViewOnError: Boolean = false,
    errorViewContent: @Composable (String) -> Unit = { SnippetErrorView(errorMessage = it, fullScreen = false) },
    displayPlaceholderWhileLoading: Boolean = false,
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(fullScreen = false) },
    interceptOverrideUrl: (String) -> Boolean = { false },
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true
) {
    val target = (managerTag?.let { TWSFactory.get(it) } ?: TWSSdk.get()).snippetsFlow.map { outcome ->
        outcome.data?.find { data ->
            data.id == name
        }
    }.collectAsStateWithLifecycle(null).value ?: return

    WebSnippetComponent(
        target = target,
        modifier = modifier,
        navigator = navigator,
        webViewState = webViewState,
        displayErrorViewOnError = displayErrorViewOnError,
        errorViewContent = errorViewContent,
        displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
        loadingPlaceholderContent = loadingPlaceholderContent,
        interceptOverrideUrl = interceptOverrideUrl,
        googleLoginRedirectUrl = googleLoginRedirectUrl,
        isRefreshable = isRefreshable
    )
}
