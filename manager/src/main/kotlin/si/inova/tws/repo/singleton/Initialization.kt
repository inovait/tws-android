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

package si.inova.tws.repo.singleton

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.squareup.moshi.Moshi
import dispatch.core.DispatcherProvider
import dispatch.core.MainImmediateCoroutineScope
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import kotlin.coroutines.cancellation.CancellationException

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

@Singleton
internal val coroutineResourceManager =
   CoroutineResourceManager(MainImmediateCoroutineScope(object : DispatcherProvider {}), provideErrorReporter)

@Singleton
internal fun twsMoshi(): Moshi {
   if (Thread.currentThread().name == "main") {
      error("Moshi should not be initialized on the main thread")
   }

   return Moshi.Builder().build()
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
