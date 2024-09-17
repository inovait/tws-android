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

package si.inova.tws.manager.factory

import com.squareup.moshi.Moshi
import dispatch.core.DefaultCoroutineScope
import dispatch.core.DispatcherProvider
import jakarta.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import si.inova.kotlinova.retrofit.callfactory.ErrorHandlingAdapterFactory
import si.inova.kotlinova.retrofit.callfactory.StaleWhileRevalidateCallAdapterFactory
import si.inova.kotlinova.retrofit.converter.LazyRetrofitConverterFactory
import si.inova.tws.manager.singleton.twsMoshi
import si.inova.tws.manager.singleton.twsOkHttpClient

@Singleton
internal class BaseServiceFactory : ServiceFactory {
    private val moshi: Moshi by lazy { twsMoshi() }
    private val okHttpClient: OkHttpClient by lazy { twsOkHttpClient() }

    override fun <S> create(
        klass: Class<S>,
        configuration: ServiceFactory.ServiceCreationScope.() -> Unit
    ): S {
        val scope = ServiceFactory.ServiceCreationScope()
        configuration(scope)

        val updatedClient = lazy {
            okHttpClient.newBuilder()
                .apply {
                    if (scope.cache) {
                        createCache()?.let { cache(it) }
                    }
                }
                .apply {
                    scope.okHttpCustomizer?.let { it() }
                }
                .build()
        }

        val moshiConverter = lazy {
            MoshiConverterFactory.create(moshi)
        }

        return Retrofit.Builder()
            .callFactory { updatedClient.value.newCall(it) }
            .baseUrl(TWS_API)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(LazyRetrofitConverterFactory(moshiConverter))
            .addCallAdapterFactory(StaleWhileRevalidateCallAdapterFactory(null))
            .addCallAdapterFactory(ErrorHandlingAdapterFactory(DefaultCoroutineScope(object : DispatcherProvider {})))
            .build()
            .create(klass)
    }

    private fun createCache(): Cache? {
        return null
    }
}

private const val TWS_API = "https://api.thewebsnippet.dev/"
