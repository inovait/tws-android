/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * This file contains modifications based on code from the Accompanist WebView wrapper.
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
