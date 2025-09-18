/*
 * Copyright 2025 INOVA IT d.o.o.
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
package com.thewebsnippet.view.client

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.view.client.okhttp.web.webViewHttpClient
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewError
import com.thewebsnippet.view.data.TWSViewInterceptor
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.util.modifier.HtmlModifierHelper
import com.thewebsnippet.view.util.modifier.HtmlModifierHelperImpl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.util.Locale

/**
 * [OkHttpTWSWebViewClient] is a specialized subclass of [TWSWebViewClient] that integrates OkHttp for
 * handling network requests efficiently and allows modification of HTML responses.
 *
 * This client overrides the default WebViewClient behavior by using OkHttp to execute network requests,
 * enabling advanced features such as:
 *
 * - **Dynamic Content Injection**: Enables injection of custom CSS and JavaScript into HTML responses via
 *   `dynamicModifiers`. Supports Mustache templating to adjust HTML content based on dynamic properties.
 * - **Caching Management**: Utilizes OkHttp's caching capabilities to improve performance, including support
 *   for handling stale content through `stale-if-error` directives.
 * - **Google Authentication Flow**: Inherits handling of specific URL redirections from [TWSWebViewClient],
 *   including the ability to open custom tabs for Google authentication flows.
 *
 * @param dynamicModifiers A list of [TWSAttachment] items used to modify the HTML content dynamically.
 * @param mustacheProps A map of properties passed to Mustache templates for HTML modification.
 * @param engine A [TWSEngine], which allows developers to skip mustache processing
 * @param interceptUrlCallback A function for intercepting and handling specific URL requests before
 * they are passed to OkHttp.
 * @param popupStateCallback An optional callback to manage visibility of popups or custom tabs within the WebView.
 * @param htmlModifier A helper used to inject CSS, JavaScript, and process HTML content through Mustache templates.
 * This enables dynamic customization of the HTML content before it's displayed in the WebView.
 */
internal class OkHttpTWSWebViewClient(
    private val dynamicModifiers: List<TWSAttachment>,
    private val mustacheProps: Map<String, Any>,
    private val engine: TWSEngine,
    interceptUrlCallback: TWSViewInterceptor,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)? = null,
    private val htmlModifier: HtmlModifierHelper = HtmlModifierHelperImpl()
) : TWSWebViewClient(interceptUrlCallback, popupStateCallback) {

    private lateinit var okHttpClient: OkHttpClient

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (state.loadingState !is TWSLoadingState.Loading) {
            state.customErrorsForCurrentRequest.clear()
        }
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        if (view == null || request == null) return null

        if (!::okHttpClient.isInitialized) {
            okHttpClient = webViewHttpClient(view.context)
        }

        if (request.method == "GET" &&
            request.isForMainFrame &&
            state.shouldInjectDynamicModifiers(request.url.toString())
        ) {
            return try {
                // Get cached or web response, depending on headers
                val response = okHttpClient.duplicateAndExecuteRequest(request)

                if (!response.isSuccessful && !response.isRedirect) {
                    throw IOException("HTTP error ${response.code}")
                }

                if (response.isRedirect) {
                    null
                } else {
                    response.modifyResponseAndServe() ?: super.shouldInterceptRequest(view, request)
                }
            } catch (e: Exception) {
                // Fallback to default behavior in case of mustache exception and expose mustache exception to developer
                state.customErrorsForCurrentRequest.add(TWSViewError.InitialLoadError(e, null))
                super.shouldInterceptRequest(view, request)
            }
        }

        // Use default behavior for other requests
        return super.shouldInterceptRequest(view, request)
    }

    private fun TWSViewState.shouldInjectDynamicModifiers(requestUrl: String): Boolean {
        return when {
            initialLoadedUrl == requestUrl -> {
                // initial page is reloaded, need to inject
                true
            }
            currentUrl == null && lastLoadedUrl == initialLoadedUrl -> {
                // current page is reloading (restoration?), but nothing is injected, since nothing was loaded before
                // inject only if SPA page
                true
            }
            else -> false
        }
    }

    private fun Response.modifyResponseAndServe(): WebResourceResponse? {
        val htmlContent = body?.getHtmlContent() ?: return null
        val modifiedHtmlContent =
            htmlModifier.modifyContent(
                htmlContent = htmlContent,
                dynamicModifiers = dynamicModifiers,
                mustacheProps = mustacheProps,
                engine = engine
            )

        val (mimeType, encoding) = getMimeTypeAndEncoding()
        return WebResourceResponse(
            mimeType,
            encoding,
            modifiedHtmlContent.byteInputStream()
        )
    }

    private fun OkHttpClient.duplicateAndExecuteRequest(request: WebResourceRequest): Response {
        val overrideRequest = Request.Builder()
            .url(request.buildUrl())
            .method(request.method, null)
            .headers(request.buildHeaders())
            .build()

        return newCall(overrideRequest).execute()
    }

    private fun Response.getMimeTypeAndEncoding(): Pair<String, String> {
        val contentType = header("Content-Type") ?: "text/html; charset=UTF-8"
        val mimeType = contentType.substringBefore(";").trim()
        val encoding = contentType.substringAfter("charset=", "UTF-8").trim()

        return Pair(mimeType, encoding)
    }

    private fun ResponseBody.getHtmlContent(): String {
        return byteStream().bufferedReader().use { it.readText() }
    }

    private fun WebResourceRequest.buildHeaders(): Headers {
        val headersMap = requestHeaders.toMutableMap()

        if (!headersMap.containsKey("Accept-Language")) {
            headersMap["Accept-Language"] = Locale.getDefault().toLanguageTag() // e.g., "en-US"
        }

        return Headers.headersOf(*headersMap.flatMap { listOf(it.key, it.value) }.toTypedArray())
    }

    private fun WebResourceRequest.buildUrl(): String = url.toString()
}
