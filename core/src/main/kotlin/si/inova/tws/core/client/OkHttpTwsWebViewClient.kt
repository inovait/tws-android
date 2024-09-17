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

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import si.inova.tws.core.data.ModifierInjectionType
import si.inova.tws.core.data.view.WebViewState

class OkHttpTwsWebViewClient(
    popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null
) : TwsWebViewClient(popupStateCallback) {
    private lateinit var okHttpClient: OkHttpClient

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (!::okHttpClient.isInitialized) {
            okHttpClient = webViewHttpClient(view.context)
        }

        if (request.method == GET_REQUEST && request.isForMainFrame) {
            return try {
                val response = okHttpClient.duplicateAndExecuteRequest(request)

                val htmlContent = response.body?.getHtmlContent() ?: return super.shouldInterceptRequest(view, request)
                val modifiedHtmlContent = htmlContent.insertCss()

                WebResourceResponse(
                    "text/html",
                    "UTF-8",
                    modifiedHtmlContent.byteInputStream()
                )
            } catch (e: Exception) {
                super.shouldInterceptRequest(view, request)
            }
        }

        // if anything occurs, fallback to default shouldInterceptRequest()
        return super.shouldInterceptRequest(view, request)
    }

    private fun ResponseBody.getHtmlContent(): String {
        return byteStream().bufferedReader().use { it.readText() }
    }

    private fun String.insertCss(): String {
        val combinedCssInjection = dynamicModifiers
            .filter { it.type == ModifierInjectionType.CSS }
            .joinToString(separator = System.lineSeparator()) { it.inject ?: "" }.trimIndent()

        return if (contains("<head>")) {
            replace(
                "</head>",
                """$combinedCssInjection</head>"""
            )
        } else {
            replace(
                "</script>",
                """</script>$combinedCssInjection"""
            )
        }
    }

    private fun OkHttpClient.duplicateAndExecuteRequest(request: WebResourceRequest): Response {
        val overrideRequest = Request.Builder()
            .url(request.buildUrl())
            .method(request.method, null)
            .headers(request.buildHeaders())
            .build()

        return newCall(overrideRequest).execute()
    }

    private fun WebResourceRequest.buildHeaders(): Headers =
        Headers.headersOf(*requestHeaders.flatMap { listOf(it.key, it.value) }.toTypedArray())

    private fun WebResourceRequest.buildUrl(): String = url.toString()
}

private const val GET_REQUEST = "GET"
