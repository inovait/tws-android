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
import si.inova.tws.core.data.TWSViewInterceptor
import si.inova.tws.core.data.TWSViewState

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

        url?.toString()?.let {
            if (it.startsWith("https://accounts.google.com")) {
                openCustomChromeTab(view.context, it)
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
}
