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

package com.thewebsnippet.view.client.okhttp

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.data.ResponseMetaData
import com.thewebsnippet.view.util.modifier.HtmlModifierHelper
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
internal class SnippetWebLoadImpl(
    private val okHttpClient: Lazy<OkHttpClient>,
    private val htmlModifier: HtmlModifierHelper
) : SnippetWebLoad {
    override fun response(snippet: TWSSnippet): ResponseMetaData {
        val request = buildRequestWithHeaders(snippet.target, snippet.headers)

        val response = okHttpClient.value.newCall(request).execute()

        val finalUrl = response.request.url.toString()

        val (mimeType, encode) = getMimeTypeAndEncoding(
            response.header("Content-Type") ?: "text/html; charset=UTF-8"
        )

        val updatedHtml = htmlModifier.modifyContent(
            response.body?.string() ?: "",
            snippet.dynamicResources,
            snippet.props,
            snippet.engine
        )

        response.close()

        return ResponseMetaData(
            url = finalUrl,
            mimeType = mimeType,
            encode = encode,
            html = updatedHtml
        )
    }

    private fun buildRequestWithHeaders(url: String, headers: Map<String, String>): Request {
        val builder = Request.Builder().url(url)
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }
        return builder.build()
    }

    private fun getMimeTypeAndEncoding(contentType: String): Pair<String, String> {
        val mimeType = contentType.substringBefore(";").trim()
        val encoding = contentType.substringAfter("charset=", "UTF-8").trim()

        return Pair(mimeType, encoding)
    }
}
