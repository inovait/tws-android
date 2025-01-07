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
package com.thewebsnippet.manager.manager.snippet

import com.thewebsnippet.manager.TWSConfiguration
import com.thewebsnippet.manager.factory.BaseServiceFactory
import com.thewebsnippet.manager.factory.create
import com.thewebsnippet.manager.function.TWSSnippetFunction
import com.thewebsnippet.manager.manager.auth.AuthLoginManagerImpl
import com.thewebsnippet.manager.preference.AuthPreference
import java.time.Instant

internal class SnippetLoadingManagerImpl(
    auth: AuthPreference,
    private val configuration: TWSConfiguration,
    private val functions: TWSSnippetFunction = BaseServiceFactory(AuthLoginManagerImpl(auth)).create()
) : SnippetLoadingManager {

    override suspend fun load(): ProjectResponse {
        val twsProjectResponse = when (configuration) {
            is TWSConfiguration.Basic -> {
                functions.getWebSnippets(projectId = configuration.projectId)
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
