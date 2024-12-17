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
package com.thewebsnippet.view.client

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewState

/**
 * AccompanistWebChromeClient is a subclass of [WebChromeClient] designed to manage
 * the web content displayed in a [WebView].
 *
 * NOTE: This is a modified version of the original Accompanist WebChromeClient implementation.
 *
 * This class provides additional features beyond the basic functionality of [WebChromeClient]
 * by allowing you to handle events such as page title updates, icon changes, and progress
 * updates for the loading state of the WebView.
 */
internal open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: TWSViewState
        internal set

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        state.title = title
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        val loadingState = state.loadingState

        if (loadingState is TWSLoadingState.Finished) return

        state.loadingState = TWSLoadingState.Loading(
            progress = newProgress / PERCENTAGE_DIVISOR,
            isUserForceRefresh = loadingState is TWSLoadingState.ForceRefreshInitiated ||
                (loadingState as? TWSLoadingState.Loading)?.isUserForceRefresh == true
        )
    }

    companion object {
        private const val PERCENTAGE_DIVISOR = 100.0f
    }
}
