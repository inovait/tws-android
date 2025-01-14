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
package com.thewebsnippet.manager.factory

import com.squareup.moshi.Moshi
import com.thewebsnippet.manager.manager.auth.Auth
import com.thewebsnippet.manager.preference.TWSBuildImpl
import com.thewebsnippet.manager.singleton.twsMoshi
import com.thewebsnippet.manager.singleton.twsOkHttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

internal class BaseServiceFactory(private val fallbackAuthentication: Auth? = null) : ServiceFactory {
    private val moshi: Moshi by lazy { twsMoshi() }
    private val okHttpClient: OkHttpClient by lazy { twsOkHttpClient(fallbackAuthentication) }

    override fun <S> create(
        klass: Class<S>,
        configuration: ServiceFactory.ServiceCreationScope.() -> Unit
    ): S {
        val scope = ServiceFactory.ServiceCreationScope()
        configuration(scope)

        val updatedClient = lazy {
            okHttpClient.newBuilder().apply {
                scope.okHttpCustomizer?.let { it() }
            }.build()
        }

        val moshiConverter = lazy {
            MoshiConverterFactory.create(moshi)
        }

        return Retrofit.Builder()
            .callFactory { updatedClient.value.newCall(it) }
            .baseUrl(TWSBuildImpl.baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(LazyRetrofitConverterFactory(moshiConverter))
            .build()
            .create(klass)
    }
}
