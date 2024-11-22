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
 * TWSViewInterceptor is a functional interface used to intercept URLs within a WebView.
 * This interface provides a single method, handleUrl, which takes a URL as input and returns
 * a Boolean indicating whether the URL has been handled by the application.
 *
 * Usage:
 * - Implement this interface to customize URL interception behavior.
 * - Use handleUrl to determine if a URL should be handled internally or externally (e.g., deep linking).
 */
fun interface TWSViewInterceptor {
    fun handleUrl(url: Uri): Boolean
}

/**
 * `TWSViewDeepLinkInterceptor` is an implementation of [TWSViewInterceptor] that handles deep link URLs
 * by launching the appropriate in-app activity if the URL matches the app's package name.
 *
 * Usage:
 * - Attach this to a `TwsWebViewClient` to intercept URLs and handle supported deep links.
 *
 * Functionality:
 * - Verifies if the URL can be handled within the app.
 * - Launches the corresponding activity if supported; otherwise, allows the WebView to load the URL.
 *
 * @param context The application context used for intent handling and URL verification.
 */
class TWSViewDeepLinkInterceptor(private val context: Context) : TWSViewInterceptor {
    override fun handleUrl(url: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url)

        val isUrlSupported = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).any {
            it.activityInfo.packageName == context.packageName
        }

        return if (isUrlSupported) {
            // Force deep link processing and mark url as handled
            startActivity(context, Intent(Intent.ACTION_VIEW, url), null)
            true
        } else {
            // Mark url as unhandled web view will display it
            false
        }
    }
}

/**
 * `TWSViewNoOpInterceptor` is a [TWSViewInterceptor] that ignores all URLs, allowing the WebView to load them by default.
 */
class TWSViewNoOpInterceptor : TWSViewInterceptor {
    override fun handleUrl(url: Uri): Boolean {
        return false
    }
}
