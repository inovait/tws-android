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

import android.webkit.CookieManager
import com.thewebsnippet.view.client.okhttp.cookie.CookieSaver
import com.thewebsnippet.view.client.okhttp.cookie.CookieSaverImpl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

internal class RedirectHandlerImpl(
    private val client: OkHttpClient,
    private val cookieSaver: CookieSaver = CookieSaverImpl(CookieManager.getInstance())
) : RedirectHandler {
    override fun execute(request: Request): Response {
        var currentRequest = request
        var response = client.newCall(currentRequest).execute()

        while (response.isRedirect) {
            val location = response.header("Location")
                ?: throw IOException("Redirect without Location header")

            cookieSaver.saveCookies(currentRequest.url, response.headers("Set-Cookie"))

            val resolvedUrl = currentRequest.url.resolve(location)
                ?: throw IOException("Failed to resolve redirect URL: $location")

            currentRequest = Request.Builder().url(resolvedUrl).build()
            response = client.newCall(currentRequest).execute()
        }

        cookieSaver.saveCookies(currentRequest.url, response.headers("Set-Cookie"))

        return response
    }
}
