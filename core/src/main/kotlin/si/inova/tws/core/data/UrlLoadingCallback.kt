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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity

/**
 * `UrlLoadingCallback` is a functional interface that defines a contract for intercepting URLs within the
 * `TwsWebViewClient` class.
 *
 * This interface provides a single abstract method, `intercept`, which takes a URL as input and returns
 * a Boolean indicating whether the URL has been handled by the application.
 *
 * Typical usage:
 * - Implement this interface to customize URL interception behavior within a WebView.
 * - Use the `intercept` method to determine if a URL should be opened within the app (e.g., as a deep link)
 *   or in an external browser.
 */
fun interface UrlLoadingCallback {
    fun intercept(url: String): Boolean
}

/**
 * `DeepLinkUrlLoadingCallback` is an implementation of [UrlLoadingCallback] designed to handle deep link URLs.
 *
 * This class intercepts URLs and attempts to open them as deep links within the app if they match the
 * app's package name. If a URL can be handled as a deep link, it triggers an intent to start the
 * activity associated with that URL.
 *
 * Example:
 * - Pass this implementation to the `TwsWebViewClient` to intercept URLs and launch corresponding
 *   activities within the app.
 *
 * @param context The application context used for launching intents and checking if URLs are supported.
 */
class DeepLinkUrlLoadingCallback(private val context: Context) : UrlLoadingCallback {
    override fun intercept(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        val isUrlSupported = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).any {
            it.activityInfo.packageName == context.packageName
        }

        return if (isUrlSupported) {
            // Force deep link processing and mark url as handled
            startActivity(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)), null)
            true
        } else {
            // Mark url as unhandled web view will display it
            false
        }
    }
}

/**
 * `NoOpLoadingCallback` is an implementation of [UrlLoadingCallback] that does not handle any URLs.
 *
 * This class is a "no-operation" implementation, meaning it will always return `false` for any URL
 * passed to its `intercept` method. It effectively instructs the `TwsWebViewClient` to allow all URLs
 * to be displayed within the WebView without additional handling.
 *
 * Usage:
 * - Use this class as a default or placeholder when no URL handling is required, or when WebView should
 *   display all URLs by default.
 */
class NoOpLoadingCallback : UrlLoadingCallback {
    override fun intercept(url: String): Boolean {
        return false
    }
}
