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

import android.content.Context
import android.webkit.CookieManager
import com.thewebsnippet.view.client.okhttp.GlobalOkHttpDiskCacheManager
import com.thewebsnippet.view.client.okhttp.cookie.SharedCookieJar
import com.thewebsnippet.view.client.okhttp.provideErrorReporter
import jakarta.inject.Singleton
import okhttp3.OkHttpClient

@Singleton
internal fun webViewHttpClient(context: Context): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread : ${Thread.currentThread().name}")
    }

    val manager = GlobalOkHttpDiskCacheManager(context, provideErrorReporter)
    val cookieManager = CookieManager.getInstance()

    return OkHttpClient.Builder()
        .cache(manager.cache)
        .cookieJar(SharedCookieJar(cookieManager))
        .followRedirects(false) // followed manually, we need that to be able to sync cookies with extensions from redirects
        .followSslRedirects(false)
        .build()
}
