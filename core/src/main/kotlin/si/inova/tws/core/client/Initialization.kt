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

import android.content.Context
import android.webkit.CookieManager
import jakarta.inject.Singleton
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager
import kotlin.coroutines.cancellation.CancellationException

@Singleton
internal fun webViewHttpClient(context: Context): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread")
    }

    return prepareDefaultOkHttpClient(context).build()
}

internal fun prepareDefaultOkHttpClient(context: Context): OkHttpClient.Builder {
    val manager = GlobalOkHttpDiskCacheManager(context, provideErrorReporter)
    val cookieManager = CookieManager.getInstance().also { it.setAcceptCookie(true) }

    return OkHttpClient.Builder()
        .cache(manager.cache)
        .cookieJar(SharedCookieJar(cookieManager))
}

@Singleton
internal val provideErrorReporter = ErrorReporter {
    object : ErrorReporter {
        override fun report(throwable: Throwable) {
            if (throwable is CancellationException) {
                report(Exception("Got cancellation exception", throwable))
                return
            }

            throwable.printStackTrace()
        }
    }
}

class SharedCookieJar(private val cookieManager: CookieManager) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookie ->
            cookieManager.setCookie(url.toString(), cookie.toString())
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieString = cookieManager.getCookie(url.toString()) ?: return emptyList()
        return cookieString.split(";").mapNotNull { cookiePart ->
            Cookie.parse(url, cookiePart.trim())
        }
    }
}