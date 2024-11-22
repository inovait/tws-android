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

package si.inova.tws.core.data

import android.os.Bundle
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
import si.inova.tws.data.TWSSnippet

/**
 * A state holder to hold the state for the WebView.
 */
@Stable
class TWSViewState(webContent: WebContent) {
    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     *  The content being loaded by the WebView
     */
    var content: WebContent by mutableStateOf(webContent)

    /**
     * The title received from the loaded content of the current page
     */
    var title: String? by mutableStateOf(null)
        internal set

    /**
     * Whether the WebView is currently [TWSLoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [TWSLoadingState.Finished]. See [TWSLoadingState]
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
    var viewState: Bundle? = null
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
 *
 */
@Composable
fun rememberTWSViewState(
    snippet: TWSSnippet
): TWSViewState =
    // Rather than using .apply {} here we will recreate the state, this prevents
    // a recomposition loop when the webview updates the url itself.
    remember(key1 = snippet) {
        TWSViewState(
            WebContent.Url(
                url = snippet.target,
                additionalHttpHeaders = snippet.headers
            )
        )
    }.apply {
        this.content = WebContent.Url(
            url = snippet.target,
            additionalHttpHeaders = snippet.headers
        )
    }

/**
 * Creates a WebView state that persists across recompositions, configured to
 * display HTML data in the TWSView.
 *
 * @param data The HTML data to load into the WebView.
 * @param baseUrl Optional base URL to resolve relative paths in the HTML content.
 * @param encoding The encoding type for the HTML content, defaulting to "utf-8".
 * @param mimeType Optional MIME type for the data; if null, "text/html" is assumed.
 * @param historyUrl Optional URL for the WebView history.
 * @param key1 An optional key used to remember the state; if changed, a new
 * instance of [TWSViewState] is created.
 *
 */
@Composable
fun rememberTWSViewStateWithHTMLData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
    key1: Any? = null
): TWSViewState =
    remember(key1 = key1) {
        TWSViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
    }.apply {
        this.content = WebContent.Data(
            data, baseUrl, encoding, mimeType, historyUrl
        )
    }

/**
 * Creates a TWSView state that persists across recompositions and is saved
 * across activity recreation. This state is remembered via a key and stored
 * using Composes `rememberSaveable` to restore state after process death.
 *
 * @param inputs A set of inputs; if any of these change, the WebView state
 * will reset.
 * @param key An optional key for saved state persistence. If not provided, the
 * key is auto-generated by Compose for each unique code location in the
 * composition tree.
 *
 * Note: When using saved state, URL updates via recomposition are disabled.
 * To load new URLs, use a TWSViewNavigator.
 */
@Composable
fun rememberSaveableTWSViewState(vararg inputs: Any?, key: String = ""): TWSViewState =
    rememberSaveable(saver = createTWSViewStateSaver(key), key = key.takeIf { it.isNotEmpty() }, inputs = inputs) {
        TWSViewState(WebContent.NavigatorOnly)
    }

private fun createTWSViewStateSaver(key: String): Saver<TWSViewState, Any> {
    val pageTitleKey = "$key:pagetitle"
    val lastLoadedUrlKey = "$key:lastloaded"
    val stateBundle = "$key:bundle"

    return mapSaver(
        save = { state ->
            val viewState = Bundle().apply {
                state.webView?.saveState(this)
            }.takeIf { !it.isEmpty } ?: state.viewState

            mapOf(
                pageTitleKey to state.title,
                lastLoadedUrlKey to state.lastLoadedUrl,
                stateBundle to viewState
            )
        },
        restore = {
            TWSViewState(WebContent.NavigatorOnly).apply {
                this.title = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
                this.viewState = it[stateBundle] as Bundle?
            }
        }
    )
}
