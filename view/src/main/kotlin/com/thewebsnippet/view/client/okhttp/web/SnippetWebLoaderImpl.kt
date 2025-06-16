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

package com.thewebsnippet.view.client.okhttp.web

import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.data.ResponseMetaData
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

@Singleton
internal class SnippetWebLoaderImpl(
    private val redirectHandler: Lazy<RedirectHandler>,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
) : SnippetWebLoader {
    override suspend fun response(snippet: TWSSnippet): ResponseMetaData = withContext(dispatcherIO) {
        val request = buildRequestWithHeaders(snippet.target, snippet.headers)
        val response = redirectHandler.value.execute(request)
        val finalUrl = response.request.url.toString()

        ResponseMetaData(url = finalUrl)
    }

    private fun buildRequestWithHeaders(url: String, headers: Map<String, String>): Request {
        val builder = Request.Builder().url(url)
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }
        return builder.build()
    }
}
