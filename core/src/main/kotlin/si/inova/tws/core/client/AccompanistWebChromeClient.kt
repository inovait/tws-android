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
import android.webkit.WebChromeClient
import android.webkit.WebView
import si.inova.tws.core.data.TWSLoadingState
import si.inova.tws.core.data.TWSViewState

/**
 * AccompanistWebChromeClient is a subclass of [WebChromeClient] designed to manage
 * the web content displayed in a [WebView].
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

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.icon = icon
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
