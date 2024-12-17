/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * This file contains modifications based on code from the Accompanist WebView library.
 * Original Copyright (c) 2021 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice, this permission notice, and the following additional notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * -----
 * Portions of this file are derived from the Accompanist WebView library,
 * which is available at https://github.com/google/accompanist/blob/main/web/src/main/java/com/google/accompanist/web/WebView.kt.
 * Copyright (c) 2021 The Android Open Source Project. Licensed under Apache License, Version 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * -----
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.thewebsnippet.view.client

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewNavigator
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.data.WebViewError

/**
 * A custom implementation of [WebViewClient] designed to manage the state and navigation of a WebView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebViewClient.
 *
 * This class serves as a bridge between the WebView and the application state, providing the necessary hooks to track
 * loading states, manage navigation history, and handle errors that may occur during page loads.
 *
 * The `AccompanistWebViewClient` class can be extended to customize its behavior for specific use cases.
 */
internal open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: TWSViewState
        internal set
    open lateinit var navigator: TWSViewNavigator
        internal set

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        state.webViewErrorsForCurrentRequest.clear()
        if (state.loadingState !is TWSLoadingState.Loading) {
            state.loadingState = TWSLoadingState.Loading(0.0f, state.loadingState is TWSLoadingState.ForceRefreshInitiated)
        }

        state.lastLoadedUrl = url
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = TWSLoadingState.Finished
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
