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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import com.thewebsnippet.view.data.TWSViewInterceptor
import com.thewebsnippet.view.data.TWSViewState

/**
 * TwsWebViewClient is a subclass of [AccompanistWebViewClient] designed to provide custom behavior for handling WebView requests.
 * It includes a mechanism to handle URLs and supports opening Google authentication flow in Custom Chrome tabs for
 * better user experience.
 *
 * Key features include:
 * - Intercepting URLs and handling specific ones, such as Google authentication URLs.
 * - Opening Google authentication URLs in Custom Chrome Tabs.
 * - Providing an optional mechanism to track the state of popups or custom tabs.
 *
 * @param interceptUrlCallback A function that intercepts URLs. It takes a URL string as input and returns a
 * Boolean indicating whether the URL has been handled by the application.
 * @param popupStateCallback An optional callback function to manage the visibility state of popups or custom tabs.
 * The callback takes two parameters: a [TWSViewState] and a Boolean. The Boolean indicates whether the
 * custom tab is open (true) or closed (false).
 */
internal open class TWSWebViewClient(
    private val interceptUrlCallback: TWSViewInterceptor,
    private val popupStateCallback: ((TWSViewState, Boolean) -> Unit)? = null
) : AccompanistWebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
        val url = request?.url

        url?.host?.let { host ->
            if (host.startsWith(REDIRECT_URL_GOOGLE) || host.startsWith(REDIRECT_URL_FACEBOOK)) {
                openCustomChromeTab(view.context, url.toString())
                return true // Indicate that we've handled the URL
            }
        }

        return request?.url?.let {
            interceptUrlCallback.handleUrl(it)
        } == true
    }

    private fun openCustomChromeTab(context: Context, url: String) {
        val mCustomTabsCallback: CustomTabsCallback = object : CustomTabsCallback() {
            override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                if (navigationEvent == TAB_HIDDEN) {
                    popupStateCallback?.invoke(state, false)
                }
            }
        }

        val mConnection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {}

            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                CustomTabsIntent.Builder(client.newSession(mCustomTabsCallback))
                    .build()
                    .launchUrl(context, Uri.parse(url))
            }
        }

        val packageName = context.getAvailablePackageName() ?: return
        CustomTabsClient.bindCustomTabsService(context, packageName, mConnection)
    }

    private fun Context.getAvailablePackageName(): String? {
        val packageName = CustomTabsClient.getPackageName(
            this,
            emptyList<String>()
        )

        return packageName ?: getAlternativePackageName()
    }

    private fun Context.getAlternativePackageName(): String? {
        // Get all apps that can handle VIEW intents and Custom Tab service connections.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http"))
        val viewIntentHandlers = packageManager.queryIntentActivities(activityIntent, 0).map { it.resolvePackageName }

        // Get a package that supports Custom Tabs
        val packageName = CustomTabsClient.getPackageName(
            this,
            viewIntentHandlers,
            true /* ignore default */
        )

        return packageName
    }

    companion object {
        private const val REDIRECT_URL_GOOGLE = "accounts.google.com"
        private const val REDIRECT_URL_FACEBOOK = "m.facebook.com"
    }
}
