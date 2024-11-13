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

package si.inova.tws.core.client

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import si.inova.tws.core.data.LoadingState
import si.inova.tws.core.data.WebViewError
import si.inova.tws.core.data.TWSViewNavigator
import si.inova.tws.core.data.TWSViewState

/**
 * A custom implementation of [WebViewClient] designed to manage the state and navigation of a WebView.
 *
 * This class serves as a bridge between the WebView and the application state, providing the necessary hooks to track
 * loading states, manage navigation history, and handle errors that may occur during page loads.
 *
 * The `AccompanistWebViewClient` class must be extended to customize its behavior for specific use cases.
 */
open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: TWSViewState
        internal set
    open lateinit var navigator: TWSViewNavigator
        internal set

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (state.loadingState !is LoadingState.Loading) {
            state.loadingState = LoadingState.Loading(0.0f, state.loadingState is LoadingState.ForceRefreshInitiated)
            state.webViewErrorsForCurrentRequest.clear()
        }

        state.lastLoadedUrl = url
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = LoadingState.Finished
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.webViewErrorsForCurrentRequest.add(WebViewError(request, error))
        }
    }
}
