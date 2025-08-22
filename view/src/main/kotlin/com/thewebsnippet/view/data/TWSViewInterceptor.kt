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
package com.thewebsnippet.view.data

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
    /**
     * @param url Request received from the web.
     * @return Should return true if the function handled the url, and false if the url should loaded in to TWSView.
     */
    fun handleUrl(url: Uri): InterceptorResult
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
    override fun handleUrl(url: Uri): InterceptorResult {
        val intent = Intent(Intent.ACTION_VIEW, url)

        val isUrlSupported = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).any {
            it.activityInfo.packageName == context.packageName
        }

        return if (isUrlSupported) {
            // Force deep link processing and mark url as handled
            startActivity(context, Intent(Intent.ACTION_VIEW, url), null)
            InterceptorResult.HANDLED_BY_USER
        } else {
            // Mark url as unhandled web view will display it
            InterceptorResult.LOAD_WEB_VIEW
        }
    }
}

/**
 * `TWSViewNoOpInterceptor` is a [TWSViewInterceptor] that ignores all URLs, allowing the WebView to load them by default.
 */
class TWSViewNoOpInterceptor : TWSViewInterceptor {
    override fun handleUrl(url: Uri): InterceptorResult {
        return InterceptorResult.LOAD_WEB_VIEW
    }
}
