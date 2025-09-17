/*
 * Copyright 2021 The Android Open Source Project
 * Modifications Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified from its original version.
 */
package com.thewebsnippet.view.data

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.client.okhttp.web.RedirectHandlerImpl
import com.thewebsnippet.view.client.okhttp.web.SnippetWebLoader
import com.thewebsnippet.view.client.okhttp.web.SnippetWebLoaderImpl
import com.thewebsnippet.view.client.okhttp.web.webViewHttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Allows control over the navigation of a WebView from outside the composable. E.g. for performing
 * a back/forward navigation in response to the user clicking the "up" or "down" button in a TopAppBar.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView navigator.
 *
 */
@Stable
class TWSViewNavigator(
    private val coroutineScope: CoroutineScope,
    private val snippetLoader: SnippetWebLoader
) {
    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    private var pendingClearHistory: Boolean = false

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    /**
     * Navigates the webview back to the previous page.
     */
    fun goBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun goForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    /**
     * Pushes a new URL to the browser history and triggers a navigation event.
     */
    fun pushState(path: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PushState(path)) }
    }

    /**
     * Replaces current URL with new URL and triggers a navigation event.
     */
    fun replaceState(path: String) {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.ReplaceState(path)) }
    }

    /**
     * Navigates back in the browser history.
     */
    fun popState() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.PopState) }
    }

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    fun loadSnippet(snippet: TWSSnippet) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.LoadSnippet(snippet))
        }
    }

    fun loadUrl(
        url: String,
        clearHistory: Boolean = false,
        behaveAsSpa: Boolean = false
    ) {
        coroutineScope.launch {
            navigationEvents.emit(NavigationEvent.LoadUrl(url, clearHistory, behaveAsSpa))
        }
    }

    internal fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtml(
                    html,
                    baseUrl,
                    mimeType,
                    encoding,
                    historyUrl
                )
            )
        }
    }

    /** Call this from WebViewClient when a page actually commits/finishes */
    internal fun maybeClearHistoryOnCommit(view: WebView) {
        if (pendingClearHistory) {
            view.clearHistory()
            pendingClearHistory = false // reset
        }
    }

    // Use Dispatchers.Main to ensure that the webview methods are called on UI thread
    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun WebView.handleNavigationEvents(
        markLoadingCallback: (Boolean) -> Unit,
        markErrorCallback: (TWSViewError.InitialLoadError) -> Unit,
        saveResolvedUrlCallback: (String) -> Unit
    ): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.Back -> goBack()
                is NavigationEvent.Forward -> goForward()
                is NavigationEvent.Reload -> reload()
                is NavigationEvent.PushState -> navigateReactOrFallback(event.path, false)
                is NavigationEvent.ReplaceState -> navigateReactOrFallback(event.path, true)
                is NavigationEvent.PopState -> {
                    @Suppress("StringTemplateIndent") // JavaScript
                    val jsScript = """
                        history.back();
                    """.trimIndent()
                    evaluateJavascript(jsScript, null)
                }

                is NavigationEvent.LoadHtml -> loadDataWithBaseURL(
                    event.baseUrl,
                    event.html,
                    event.mimeType,
                    event.encoding,
                    event.historyUrl
                )

                is NavigationEvent.LoadUrl -> {
                    resolveRedirectAndLoad(
                        event.url,
                        emptyMap(),
                        markLoadingCallback,
                        markErrorCallback,
                        { if (event.behaveAsSpa) saveResolvedUrlCallback(it) },
                        event.clearHistory
                    )
                }

                is NavigationEvent.LoadSnippet -> {
                    resolveRedirectAndLoad(
                        event.snippet.target,
                        event.snippet.headers,
                        markLoadingCallback,
                        markErrorCallback,
                        saveResolvedUrlCallback
                    )
                }
            }

            navigationEvents.resetReplayCache()
        }
    }

    @Suppress("SwallowedException") // swallow cancellation exception
    private suspend fun WebView.resolveRedirectAndLoad(
        target: String,
        headers: Map<String, String>,
        markLoadingCallback: (Boolean) -> Unit,
        markErrorCallback: (TWSViewError.InitialLoadError) -> Unit,
        saveResolvedUrlCallback: (String) -> Unit,
        clearHistory: Boolean = false
    ) {
        markLoadingCallback(true)

        try {
            val response = snippetLoader.response(target, headers)
            saveResolvedUrlCallback(response.url)
            loadUrl(response.url)

            pendingClearHistory = clearHistory
        } catch (e: CancellationException) {
            // ignore cancellation exception
        } catch (e: Exception) {
            markLoadingCallback(false)
            markErrorCallback(TWSViewError.InitialLoadError(e, null))
        }
    }

    private fun WebView.navigateReactOrFallback(path: String, replace: Boolean) {
        val checkNavigateFunction = "typeof window.navigateFromNative === 'function';"
        evaluateJavascript(checkNavigateFunction) { result ->
            if (result == "true") {
                val jsNavigate = "navigateFromNative('$path', { replace: $replace });"
                evaluateJavascript(jsNavigate, null)
            } else {
                val historyMethod = if (replace) "replaceState" else "pushState"

                @Suppress("StringTemplateIndent") // JavaScript
                val fallbackNavigate = """
                    history.$historyMethod(null, "", '$path');
                    window.dispatchEvent(new Event("popstate"));
                """.trimIndent()
                evaluateJavascript(fallbackNavigate, null)
            }
        }
    }

    private sealed interface NavigationEvent {
        data object Back : NavigationEvent
        data object Forward : NavigationEvent
        data object Reload : NavigationEvent

        data object PopState : NavigationEvent
        data class PushState(val path: String) : NavigationEvent
        data class ReplaceState(val path: String) : NavigationEvent

        data class LoadSnippet(val snippet: TWSSnippet) : NavigationEvent
        data class LoadUrl(
            val url: String,
            val clearHistory: Boolean,
            val behaveAsSpa: Boolean
        ) : NavigationEvent

        data class LoadHtml(
            val html: String,
            val baseUrl: String? = null,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null
        ) : NavigationEvent
    }
}

/**
 * Creates and remembers a [TWSViewNavigator] with the default [CoroutineScope].
 *
 * @param coroutineScope Optional scope for coroutine operations. Defaults to [rememberCoroutineScope].
 * @return A remembered instance of [TWSViewNavigator].
 */
@Composable
fun rememberTWSViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): TWSViewNavigator {
    val context = LocalContext.current

    return remember(coroutineScope) {
        val redirectionHandler = lazy {
            RedirectHandlerImpl(webViewHttpClient(context))
        }

        TWSViewNavigator(
            coroutineScope = coroutineScope,
            snippetLoader = SnippetWebLoaderImpl(redirectionHandler)
        )
    }
}

/**
 * Creates and remembers a [TWSViewNavigator] with an optional key and default [CoroutineScope].
 *
 * @param key1 An optional key used for recomposition.
 * @param coroutineScope Optional scope for coroutine operations. Defaults to [rememberCoroutineScope].
 * @return A remembered instance of [TWSViewNavigator].
 */
@Composable
fun rememberTWSViewNavigator(
    key1: Any?,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): TWSViewNavigator {
    val context = LocalContext.current

    return remember(key1) {
        val redirectionHandler = lazy {
            RedirectHandlerImpl(webViewHttpClient(context))
        }
        TWSViewNavigator(
            coroutineScope = coroutineScope,
            snippetLoader = SnippetWebLoaderImpl(redirectionHandler)
        )
    }
}
