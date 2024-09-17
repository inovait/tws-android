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

package si.inova.tws.manager.singleton

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_INSTANT

@Singleton
internal fun twsMoshi(): Moshi {
    if (Thread.currentThread().name == "main") {
        error("Moshi should not be initialized on the main thread")
    }

    return Moshi.Builder()
        .add(InstantJsonAdapter())
        .build()
}

@Singleton
internal fun twsOkHttpClient(): OkHttpClient {
    if (Thread.currentThread().name == "main") {
        error("OkHttp should not be initialized on the main thread")
    }

    return prepareDefaultOkHttpClient().build()
}

internal fun prepareDefaultOkHttpClient(): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .addInterceptor(BypassCacheInterceptor())
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