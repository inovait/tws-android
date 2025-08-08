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
package com.thewebsnippet.manager.data.setup

import android.content.Context
import android.webkit.WebSettings
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.thewebsnippet.data.TWSAttachmentType
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.manager.domain.auth.Auth
import com.thewebsnippet.manager.data.preference.TWSBuildImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
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

internal fun twsOkHttpClient(context: Context, fallbackAuthentication: Auth?): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread")
    }

    return prepareBaseOkHttpClient(context, fallbackAuthentication).build()
}

internal fun prepareBaseOkHttpClient(context: Context, auth: Auth?): OkHttpClient.Builder {
    val userAgentInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", WebSettings.getDefaultUserAgent(context).toTWSUserAgent())
            .build()
        chain.proceed(requestWithUserAgent)
    }

    return OkHttpClient.Builder()
        .retryOnConnectionFailure(false)
        .apply {
            addInterceptor(userAgentInterceptor)
            addInterceptor(prepareAuthInterceptor(auth))
            authenticator(prepareAuthenticator(auth))
        }
}

private fun prepareAuthenticator(auth: Auth?) = Authenticator { _, response ->
    runBlocking {
        val token: String? = if (auth != null) {
            auth.refreshToken()
            auth.getToken.firstOrNull()?.takeIf { it.isNotBlank() }
        } else {
            TWSBuildImpl.token
        }

        if (token.isNullOrBlank()) {
            // Stop retrying if token is invalid or empty
            null
        } else {
            response.request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
    }
}

private fun prepareAuthInterceptor(auth: Auth?) = Interceptor { chain ->
    val original = chain.request()

    val token: String = runBlocking {
        if (auth != null) {
            val token = auth.getToken.firstOrNull()?.takeIf { it.isNotBlank() }
            if (token == null) {
                auth.refreshToken()
                auth.getToken.first()
            } else {
                token
            }
        } else {
            TWSBuildImpl.token
        }
    }

    chain.proceed(
        original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    )
}

internal fun String.toTWSUserAgent() = "$this TheWebSnippet"

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
