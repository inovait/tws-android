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
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.samskivert.mustache.MustacheException
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import si.inova.tws.core.client.okhttp.webViewHttpClient
import si.inova.tws.core.data.LoadingState
import si.inova.tws.core.data.TWSInterceptUrlCallback
import si.inova.tws.core.data.TWSViewState
import si.inova.tws.core.util.HtmlModifierHelper
import si.inova.tws.data.TWSAttachment
import si.inova.tws.data.TWSEngine
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
 */
class OkHttpTWSWebViewClient(
    private val dynamicModifiers: List<TWSAttachment>,
    private val mustacheProps: Map<String, Any>,
    private val engine: TWSEngine,
    interceptUrlCallback: TWSInterceptUrlCallback,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)? = null
) : TWSWebViewClient(interceptUrlCallback, popupStateCallback) {

    private lateinit var okHttpClient: OkHttpClient
    private val htmlModifier = HtmlModifierHelper()

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        if (state.loadingState !is LoadingState.Loading) {
            state.customErrorsForCurrentRequest.clear()
        }
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (!::okHttpClient.isInitialized) {
            okHttpClient = webViewHttpClient(view.context)
        }

        if (request.method == "GET" && request.isForMainFrame) {
            return try {
                // Get cached or web response, depending on headers
                val response = okHttpClient.duplicateAndExecuteRequest(request)

                if (response.isRedirect) {
                    null
                } else {
                    response.modifyResponseAndServe() ?: super.shouldInterceptRequest(view, request)
                }
            } catch (e: Exception) {
                if (e is MustacheException) {
                    // Fallback to default behavior in case of mustache exception and expose mustache exception to developer
                    state.customErrorsForCurrentRequest.add(e)
                    super.shouldInterceptRequest(view, request)
                } else {
                    // Exception, get stale-if-error header and check if cache is still valid
                    okHttpClient.duplicateAndExecuteCachedRequest(request)?.modifyResponseAndServe()?.also {
                        state.customErrorsForCurrentRequest.add(e)
                    } ?: super.shouldInterceptRequest(view, request)
                }
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
