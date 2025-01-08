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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dispatch.core.IOCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class AuthPreferenceImpl(
    private val authPreferences: DataStore<Preferences>,
    scope: CoroutineScope = IOCoroutineScope()
) : AuthPreference {

    private val storedJwt: Flow<String> by lazy {
        authPreferences.data
            .map { preferences ->
                preferences[DATASTORE_JWT] ?: ""
            }
    }

    init {
        scope.launch {
            val currentJwt = storedJwt.first()
            if (currentJwt != JWT.token && currentJwt.isNotEmpty()) {
                clearPreferences()
            }

            setJWT(JWT.token)
        }
    }

    override val refreshToken: Flow<String> by lazy {
        authPreferences.data
            .map { preferences ->
                preferences[DATASTORE_REFRESH_TOKEN] ?: ""
            }
    }

    override val accessToken: Flow<String> by lazy {
        authPreferences.data
            .map { preferences ->
                preferences[DATASTORE_ACCESS_TOKEN] ?: ""
            }
    }

    override suspend fun setRefreshToken(refreshToken: String) {
        authPreferences.edit { settings ->
            settings[DATASTORE_REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun setAccessToken(accessToken: String) {
        authPreferences.edit { settings ->
            settings[DATASTORE_ACCESS_TOKEN] = accessToken
        }
    }

    private suspend fun setJWT(jwt: String) {
        authPreferences.edit { settings ->
            settings[DATASTORE_JWT] = jwt
        }
    }

    private suspend fun clearPreferences() {
        authPreferences.edit {
            it.clear()
        }
    }

    internal companion object {
        val DATASTORE_JWT = stringPreferencesKey("jwt")
        val DATASTORE_REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        val DATASTORE_ACCESS_TOKEN = stringPreferencesKey("accessToken")
    }
}
