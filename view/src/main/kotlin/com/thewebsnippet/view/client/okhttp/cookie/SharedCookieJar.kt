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
package com.thewebsnippet.view.client.okhttp.cookie

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * SharedCookieJar manages cookies for OkHttp using a shared CookieManager, allowing synchronization between
 * OkHttp requests and Android's WebView for consistent session handling.
 */
internal class SharedCookieJar(private val cookieManager: CookieManager) : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // Saving is done manually in ManualRedirectCookieClient, since CookieJar does not support SameSite extension
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookieString = cookieManager.getCookie(url.toString()) ?: return emptyList()
        return cookieString.split(";").mapNotNull { cookiePart ->
            Cookie.parse(url, cookiePart.trim())
        }
    }
}
