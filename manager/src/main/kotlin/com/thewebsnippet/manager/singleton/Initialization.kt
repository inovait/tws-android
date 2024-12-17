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
package com.thewebsnippet.manager.singleton

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.manager.manager.auth.Auth
import com.thewebsnippet.manager.preference.AuthPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_INSTANT

internal fun twsMoshi(): Moshi {
    if (Thread.currentThread().name == "main") {
        error("Moshi should not be initialized on the main thread")
    }

    return Moshi.Builder()
        .add(InstantJsonAdapter())
        .add(TWSEngine::class.java, EnumJsonAdapter.create(TWSEngine::class.java).withUnknownFallback(TWSEngine.OTHER))
        .add(
            TWSAttachmentType::class.java,
            EnumJsonAdapter.create(TWSAttachmentType::class.java).withUnknownFallback(TWSAttachmentType.OTHER)
        )
        .build()
}

internal fun twsOkHttpClient(fallbackAuthentication: Auth?): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread")
    }

    return prepareBaseOkHttpClient(fallbackAuthentication).build()
}

internal fun prepareBaseOkHttpClient(auth: Auth?): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .apply {
            addInterceptor { chain ->
                runBlocking {
                    val token = auth?.getToken?.first() ?: AuthPreference.jwt

                    val request = chain.request()
                        .newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()

                    chain.proceed(request)
                }
            }
            if (auth != null) {
                authenticator { _, response ->
                    runBlocking {
                        auth.refreshToken()

                        val token = auth.getToken.first()

                        response.request.newBuilder()
                            .header("Authorization", "Bearer $token")
                            .build()
                    }
                }
            }
        }
        .addNetworkInterceptor(certificateTransparencyInterceptor())
}

internal class InstantJsonAdapter {
    private val formatter: DateTimeFormatter = ISO_INSTANT.withZone(ZoneOffset.UTC)

    @ToJson
    fun toJson(instant: Instant): String {
        return formatter.format(instant)
    }

    @FromJson
    fun fromJson(timestamp: String): Instant {
        return Instant.from(formatter.parse(timestamp))
    }
}
