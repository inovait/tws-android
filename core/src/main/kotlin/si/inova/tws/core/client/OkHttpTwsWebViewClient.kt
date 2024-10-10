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
import okhttp3.CacheControl
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import si.inova.tws.core.data.WebViewState
import si.inova.tws.core.client.okhttp.webViewHttpClient
import si.inova.tws.core.data.LoadingState
import si.inova.tws.data.DynamicResourceDto
import si.inova.tws.data.ModifierInjectionType
import java.util.concurrent.TimeUnit

/**
 * OkHttpTwsWebViewClient is a specialized subclass of [TwsWebViewClient] that integrates
 * OkHttp for efficient HTTP request handling and response modification.
 *
 * This client overrides the default behavior of WebViewClient to use OkHttp for executing
 * network requests. It supports dynamic content injection into HTML responses, manages
 * caching behavior to enhance performance and sync cookie store with default WebView's cookie store..
 *
 * @param popupStateCallback An optional callback that handles the visibility state of popups
 * in the WebView.
 */
class OkHttpTwsWebViewClient(
    popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null
) : TwsWebViewClient(popupStateCallback) {
    lateinit var dynamicModifiers: List<DynamicResourceDto>
        internal set

    private lateinit var okHttpClient: OkHttpClient

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

        if (request.method == GET_REQUEST && request.isForMainFrame) {
            return try {
                // Get cached or web response, depending on headers
                val response = okHttpClient.duplicateAndExecuteRequest(request)

                if (response.isRedirect) {
                    null
                } else {
                    response.modifyResponseAndServe() ?: super.shouldInterceptRequest(view, request)
                }
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
        val modifiedHtmlContent = htmlContent.insertCss().insertJs()

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

    private fun String.insertCss(): String {
        val combinedCssInjection = dynamicModifiers
            .filter { it.type == ModifierInjectionType.CSS }
            .joinToString(separator = System.lineSeparator()) { it.inject ?: "" }.trimIndent()

        return if (contains("</head>")) {
            replaceFirst(
                "</head>",
                """$combinedCssInjection</head>"""
            )
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedCssInjection""")
            } else {
                "$combinedCssInjection$this"
            }
        }
    }

    private fun String.insertJs(): String {
        val combinedJsInjection = dynamicModifiers
            .filter { it.type == ModifierInjectionType.JAVASCRIPT }
            .joinToString(separator = System.lineSeparator()) { (it.inject ?: "") + STATIC_INJECT_DATA }.trimIndent()

        return if (contains("<head>")) {
            replaceFirst(
                "<head>",
                """<head>$combinedJsInjection"""
            )
        } else {
            val htmlTagRegex = Regex("<html(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
            if (htmlTagRegex.containsMatchIn(this)) {
                replaceFirst(htmlTagRegex, """$0$combinedJsInjection""")
            } else {
                "$combinedJsInjection$this"
            }
        }
    }

    private fun WebResourceRequest.buildHeaders(): Headers =
        Headers.headersOf(*requestHeaders.flatMap { listOf(it.key, it.value) }.toTypedArray())

    private fun WebResourceRequest.buildUrl(): String = url.toString()
}

private const val GET_REQUEST = "GET"
private val STATIC_INJECT_DATA = listOf("""<script type="text/javascript">var tws_injected = true;</script>""".trimIndent())
