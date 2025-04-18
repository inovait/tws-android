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

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.samskivert.mustache.MustacheException
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.view.client.okhttp.InjectionFilterCallback
import com.thewebsnippet.view.client.okhttp.webViewHttpClient
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewInterceptor
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.util.modifier.HtmlModifierHelper
import com.thewebsnippet.view.util.modifier.HtmlModifierHelperImpl
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit

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
 * @param injectionFilterCallback A filter to determine which GET requests should be intercepted and modified.
 * Defaults to [NoOpInjectionFilter], which allows injection by default.
 */
internal class OkHttpTWSWebViewClient(
    private val dynamicModifiers: List<TWSAttachment>,
    private val mustacheProps: Map<String, Any>,
    private val engine: TWSEngine,
    interceptUrlCallback: TWSViewInterceptor,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)? = null,
    private val htmlModifier: HtmlModifierHelper = HtmlModifierHelperImpl(),
    private val injectionFilterCallback: InjectionFilterCallback = NoOpInjectionFilter
) : TWSWebViewClient(interceptUrlCallback, popupStateCallback) {

    private lateinit var okHttpClient: OkHttpClient

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (state.loadingState !is TWSLoadingState.Loading) {
            state.customErrorsForCurrentRequest.clear()
        }
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (!::okHttpClient.isInitialized) {
            okHttpClient = webViewHttpClient(view.context)
        }

        if (request.method == "GET" && request.isForMainFrame && injectionFilterCallback.invoke(request)) {
            return try {
                // Get cached or web response, depending on headers
                val response = okHttpClient.duplicateAndExecuteRequest(request)

                if (response.isRedirect) {
                    null
                } else {
                    response.modifyResponseAndServe() ?: super.shouldInterceptRequest(view, request)
                }
            } catch (e: MustacheException) {
                // Fallback to default behavior in case of mustache exception and expose mustache exception to developer
                state.customErrorsForCurrentRequest.add(e)
                super.shouldInterceptRequest(view, request)
            } catch (e: Exception) {
                // Exception, get stale-if-error header and check if cache is still valid
                okHttpClient.duplicateAndExecuteCachedRequest(request)?.modifyResponseAndServe()?.also {
                    state.customErrorsForCurrentRequest.add(e)
                } ?: super.shouldInterceptRequest(view, request)
            }
        }

        // Use default behavior for other requests
        return super.shouldInterceptRequest(view, request)
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

    private fun OkHttpClient.duplicateAndExecuteCachedRequest(request: WebResourceRequest): Response? {
        // Build a forced cache request
        val overrideRequest = Request.Builder()
            .url(request.buildUrl())
            .method(request.method, null)
            .cacheControl(CacheControl.FORCE_CACHE)
            .build()

        // Get the cached response
        val cachedResponse = newCall(overrideRequest).execute()

        // Extract and parse stale-if-error max age from Cache-Control header
        val staleIfErrorMaxAge = cachedResponse.header("cache-Control")
            ?.split(',')
            ?.firstOrNull { it.trim().startsWith("stale-if-error") }
            ?.substringAfter("=")
            ?.toIntOrNull() ?: return null

        // Parse the Date header
        val responseDate = cachedResponse.headers.getDate("date")?.time ?: return null

        // Calculate the current age
        val currentAge = cachedResponse.header("age")?.toLongOrNull()?.let { ageHeader ->
            ageHeader + TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - responseDate))
        } ?: TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - responseDate)

        // Return the cached response if within stale-if-error max age, otherwise null
        return if (currentAge <= staleIfErrorMaxAge) cachedResponse else null
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

    private fun WebResourceRequest.buildHeaders(): Headers =
        Headers.headersOf(*requestHeaders.flatMap { listOf(it.key, it.value) }.toTypedArray())

    private fun WebResourceRequest.buildUrl(): String = url.toString()
}

// NoOp default injection filter, which allows all requests to be injected and modified
val NoOpInjectionFilter = InjectionFilterCallback { true }
