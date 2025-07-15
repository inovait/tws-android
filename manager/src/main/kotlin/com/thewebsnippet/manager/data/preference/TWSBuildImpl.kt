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

package com.thewebsnippet.manager.data.preference

import android.content.Context
import com.thewebsnippet.manager.domain.preference.TWSBuild

internal object TWSBuildImpl : TWSBuild {
    private lateinit var appContext: Context

    override fun safeInit(context: Context) {
        if (TWSBuildImpl::appContext.isInitialized) return

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
