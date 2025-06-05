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
package com.thewebsnippet.manager.data.factory

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Wrapper around [Converter.Factory] that initializes the factory lazily on first request.
 *
 * This allows factory to be initialized lazily on the background thread instead of eagerly on the main thread on the app
 * startup, improving the user experience.
 *
 * This wrapper only works for converter factories that handle all values (such as Moshi converter factory).
 */
internal class LazyRetrofitConverterFactory(private val parentFactory: Lazy<Converter.Factory>) : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val lazyConverter = lazy {
            requireNotNull(
                parentFactory.value.responseBodyConverter(
                    type,
                    annotations,
                    retrofit
                )
            ) { "Moshi converter should never be null" }
        }

        return Converter { lazyConverter.value.convert(it) }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val lazyConverter = lazy {
            @Suppress("UNCHECKED_CAST")
            parentFactory.value.requestBodyConverter(
                type,
                parameterAnnotations,
                methodAnnotations,
                retrofit
            ) as? Converter<Any?, RequestBody> ?: error("Moshi converter should never be null")
        }

        return Converter<Any?, RequestBody> { value -> lazyConverter.value.convert(value) }
    }
}
