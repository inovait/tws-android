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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.thewebsnippet.manager.TWSConfiguration
import com.thewebsnippet.manager.data.ProjectDto
import com.thewebsnippet.manager.factory.BaseServiceFactory
import com.thewebsnippet.manager.factory.create
import com.thewebsnippet.manager.function.TWSSnippetFunction
import com.thewebsnippet.manager.manager.auth.AuthLoginManagerImpl
import com.thewebsnippet.manager.preference.AuthPreference
import com.thewebsnippet.manager.preference.AuthPreferenceImpl
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant

internal class SnippetLoadingManagerImpl(
    context: Context,
    private val configuration: TWSConfiguration,
    private val authPreference: AuthPreference = AuthPreferenceImpl(context.authPreferences),
    private val functions: TWSSnippetFunction = BaseServiceFactory(AuthLoginManagerImpl(authPreference)).create()
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

        val body = twsProjectResponse.body() ?: error("Body not available")

        return ProjectResponse(
            body.injectAuthHeader(),
            twsProjectResponse.headers().getDate(HEADER_DATE)?.toInstant() ?: Instant.now()
        )
    }

    private suspend fun ProjectDto.injectAuthHeader(): ProjectDto {
        authPreference.accessToken.firstOrNull()?.takeIf { it.isNotBlank() }?.let { token ->
            val header = HEADER_TWS_AUTH to token.convertToBearerToken()

            val injectedSnippets = snippets.map { snippet ->
                snippet.copy(
                    headers = snippet.headers?.let {
                        it + header
                    } ?: mapOf(header)
                )
            }

            return copy(snippets = injectedSnippets)
        } ?: return this
    }

    private fun String.convertToBearerToken() = "$BEARER $this"

    companion object {
        private const val HEADER_DATE = "date"
        private const val HEADER_TWS_AUTH = "x-tws-access-token"
        private const val BEARER = "Bearer"
    }
}

private const val DATASTORE_NAME = "authPreferences"
private val Context.authPreferences: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
