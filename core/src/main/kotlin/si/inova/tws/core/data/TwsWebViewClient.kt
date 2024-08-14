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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection

/**
 * TwsWebViewClient, copied from AccompanistWebViewClient to allow further modifications
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overridden if further custom behaviour is required.
 */
open class TwsWebViewClient(private val popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null) : WebViewClient() {
   open lateinit var state: WebViewState
      internal set
   open lateinit var navigator: WebViewNavigator
      internal set
   open lateinit var interceptOverrideUrl: (String) -> Boolean
      internal set
   open lateinit var injectPage: List<String>
      internal set

   override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
      val url = request?.url

      url?.toString()?.let {
         if (it.startsWith("https://accounts.google.com")) {
            openCustomChromeTab(view.context, it)
            return true // Indicate that we've handled the URL
         }
      }

      return request?.url?.let {
         interceptOverrideUrl(it.toString())
      } == true
   }

   override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
      super.onPageStarted(view, url, favicon)
      state.loadingState = LoadingState.Loading(0.0f)
      state.errorsForCurrentRequest.clear()

      state.lastLoadedUrl = url
   }

   override fun onPageFinished(view: WebView, url: String?) {
      super.onPageFinished(view, url)

      Handler(Looper.getMainLooper()).postDelayed({
         state.loadingState = LoadingState.Finished
      }, DELAY_LOADING_MS)

      injectPage.forEach {
         view.evaluateJavascript(it, null)
      }
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
         state.errorsForCurrentRequest.add(WebViewError(request, error))
      }
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

private const val DELAY_LOADING_MS = 300L
