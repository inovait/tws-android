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
package com.thewebsnippet.view.client.okhttp

import android.content.Context
import android.webkit.CookieManager
import jakarta.inject.Singleton
import okhttp3.OkHttpClient

@Singleton
internal fun webViewHttpClient(context: Context): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread")
    }

    val manager = GlobalOkHttpDiskCacheManager(context, provideErrorReporter)
    val cookieManager = CookieManager.getInstance().also { it.setAcceptCookie(true) }

    return OkHttpClient.Builder()
        .cache(manager.cache)
        .cookieJar(SharedCookieJar(cookieManager))
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
}
