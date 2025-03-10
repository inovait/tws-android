/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.preference

import android.content.Context

internal object TWSBuildImpl : TWSBuild {
    private lateinit var appContext: Context

    override fun safeInit(context: Context) {
        if (::appContext.isInitialized) return

        appContext = context
    }

    override val token: String by lazy {
        appContext.getString(appContext.resources.getIdentifier(RESOURCE_VALUE_JWT, "string", appContext.packageName))
    }

    override val baseUrl: String by lazy {
        appContext.getString(appContext.resources.getIdentifier(RESOURCE_VALUE_BASE_URL, "string", appContext.packageName))
    }

    private const val RESOURCE_VALUE_JWT = "com.thewebsnippet.service.jwt"
    private const val RESOURCE_VALUE_BASE_URL = "com.thewebsnippet.service.base.url"
}
