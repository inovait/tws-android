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

package com.thewebsnippet.manager.manager.snippet

import com.thewebsnippet.manager.TWSConfiguration
import com.thewebsnippet.manager.factory.BaseServiceFactory
import com.thewebsnippet.manager.factory.create
import com.thewebsnippet.manager.function.TWSSnippetFunction
import com.thewebsnippet.manager.manager.auth.AuthLoginManagerImpl
import java.time.Instant

internal class SnippetLoadingManagerImpl(
    private val configuration: TWSConfiguration,
    private val functions: TWSSnippetFunction = BaseServiceFactory(AuthLoginManagerImpl()).create()
) : SnippetLoadingManager {

    override suspend fun load(): ProjectResponse {
        val twsProjectResponse = when (configuration) {
            is TWSConfiguration.Basic -> {
                functions.getWebSnippets(organizationId = configuration.organizationId, projectId = configuration.projectId)
            }

            is TWSConfiguration.Shared -> {
                val sharedSnippet = functions.getSharedSnippetToken(configuration.sharedId)
                functions.getSharedSnippetData(sharedSnippet.shareToken)
            }
        }

        return ProjectResponse(
            twsProjectResponse.body() ?: error("Body not available"),
            twsProjectResponse.headers().getDate(HEADER_DATE)?.toInstant() ?: Instant.now()
        )
    }

    companion object {
        private const val HEADER_DATE = "date"
    }
}
