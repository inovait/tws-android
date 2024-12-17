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

import okhttp3.OkHttpClient

internal interface ServiceFactory {
    fun <S> create(klass: Class<S>, configuration: ServiceCreationScope.() -> Unit = {}): S
    class ServiceCreationScope {
        var okHttpCustomizer: (OkHttpClient.Builder.() -> Unit)? = null

        var cache: Boolean = true
    }
}

internal inline fun <reified S> ServiceFactory.create(
    noinline configuration: ServiceFactory.ServiceCreationScope.() -> Unit = { }
): S {
    return create(S::class.java, configuration)
}
