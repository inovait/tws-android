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

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.saver.FileWebViewStateManager
import com.thewebsnippet.view.saver.WebViewStateManager

/**
 * A state holder to hold the state for the WebView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView state holder.
 * Modifications made include:
 * - Added custom error tracking with `customErrorsForCurrentRequest`.
 * - Removed some of the properties that were unused in our implementation.
 */
@Stable
class TWSViewState(webContent: WebContent) {
    /**
     *  The content being loaded by the WebView
     */
    var content: WebContent by mutableStateOf(webContent)

    /**
     * The URL of the last page that started loading.
     * This is updated when a new page load begins.
     */
    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     * The URL of the most recently visited page in the browsing history.
     * This is updated as the user navigates, including when a page is reloaded.
     * Used in Single Page Applications (SPA) to reflect URL changes.
     */
    var currentUrl: String? by mutableStateOf(null)
        internal set

    /**
     * The URL of the initial page that started loading.
     */
    var initialLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     * The title received from the loaded content of the current page
     */
    var title: String? by mutableStateOf(null)
        internal set

    /**
     * Whether the WebView is currently loading resources and loading its main frame (along with
     * progress). See [TWSLoadingState]
     */
    var loadingState: TWSLoadingState by mutableStateOf(TWSLoadingState.Initializing)

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     */
    val webViewErrorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be only from the main page.
     */
    val customErrorsForCurrentRequest: SnapshotStateList<Exception> = mutableStateListOf()

    /**
     * The saved view state from when the view was destroyed last. To restore state,
     * use the navigator and only call loadUrl if the bundle is null.
     */
    var viewStatePath: String? = null
        internal set

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    var webView by mutableStateOf<WebView?>(null)
        internal set
}

/**
 * Creates a TWSView state that persists across recompositions, configured to display URL with
 * optional additional headers.
 *
 * @param snippet An instance of [TWSSnippet] containing the target URL and any
 * additional HTTP headers for the WebView.
 */
@Composable
fun rememberTWSViewState(
    snippet: TWSSnippet
): TWSViewState =
    // Rather than using .apply {} here we will recreate the state, this prevents
    // a recomposition loop when the webview updates the url itself.
    remember(key1 = snippet) {
        TWSViewState(
            WebContent.Snippet(target = snippet)
        )
    }.apply {
        content = WebContent.Snippet(target = snippet)
    }

/**
 * Creates a TWSView state that persists across recompositions and is saved
 * across activity recreation. This state is remembered via a key and stored
 * using Composes `rememberSaveable` to restore state after process death.
 *
 * @param inputs A set of inputs to determine when the state should be reset.
 * @param default A [TWSSnippet] containing the URL, custom HTTP headers, and modifiers
 * for the web snippet to be rendered.
 * @param key An optional key for saved state persistence; defaults to an auto-generated key.
 * @return A [TWSViewState] that retains its state across activity recreation.
 *
 * Note: When using saved state, URL updates via recomposition are disabled.
 * To load new URLs, use a TWSViewNavigator.
 */
@Composable
fun rememberSavableTWSViewState(
    vararg inputs: Any?,
    default: TWSSnippet? = null,
    key: String = default?.id.orEmpty()
): TWSViewState {
    return rememberSaveable(
        saver = createTWSViewStateSaver(LocalContext.current, default),
        key = key.takeIf { it.isNotEmpty() },
        inputs = inputs
    ) {
        TWSViewState(WebContent.NavigatorOnly(default = default))
    }
}

private fun createTWSViewStateSaver(
    context: Context,
    default: TWSSnippet?,
    stateManager: WebViewStateManager = FileWebViewStateManager()
): Saver<TWSViewState, Any> {
    val key = default?.id.orEmpty()

    val pageTitleKey = "$key:pagetitle"
    val lastLoadedUrlKey = "$key:lastloaded"
    val initialLoadedUrlKey = "$key:initialLoaded"
    val bundlePath = "$key:path"

    return mapSaver(
        save = { state ->
            val storePath = state.webView?.let {
                stateManager.saveWebViewState(context, it, key)
            }

            mapOf(
                pageTitleKey to state.title,
                lastLoadedUrlKey to state.lastLoadedUrl,
                initialLoadedUrlKey to state.initialLoadedUrl,
                bundlePath to storePath
            )
        },
        restore = {
            TWSViewState(WebContent.NavigatorOnly(default)).apply {
                this.title = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
                this.viewStatePath = it[bundlePath] as String?
                this.initialLoadedUrl = it[initialLoadedUrlKey] as String?
            }
        }
    )
}
