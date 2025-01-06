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
package com.thewebsnippet.manager.preference

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@SuppressLint("DiscouragedApi") // RESOURCE_VALUE_NAME can only be retrieved from build
internal object AuthPreference {
    private lateinit var appContext: Context

    private val Context.authPreferences: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

    fun safeInit(context: Context) {
        if (::appContext.isInitialized) return

        appContext = context

        runBlocking {
            if (storedJwt.first() != jwt && storedJwt.first().isNotEmpty()) {
                clearPreferences()
            }

            setJWT(jwt)
        }
    }

    val jwt: String by lazy {
        appContext.getString(appContext.resources.getIdentifier(RESOURCE_VALUE_NAME, "string", appContext.packageName))
    }

    val refreshToken: Flow<String> by lazy {
        appContext.authPreferences.data
            .map { preferences ->
                preferences[REFRESH_TOKEN] ?: ""
            }
    }

    val accessToken: Flow<String> by lazy {
        appContext.authPreferences.data
            .map { preferences ->
                preferences[ACCESS_TOKEN] ?: ""
            }
    }

    suspend fun setRefreshToken(refreshToken: String) {
        appContext.authPreferences.edit { settings ->
            settings[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun setAccessToken(accessToken: String) {
        appContext.authPreferences.edit { settings ->
            settings[ACCESS_TOKEN] = accessToken
        }
    }

    private val storedJwt: Flow<String> by lazy {
        appContext.authPreferences.data
            .map { preferences ->
                preferences[JWT] ?: ""
            }
    }

    private suspend fun setJWT(jwt: String) {
        appContext.authPreferences.edit { settings ->
            settings[JWT] = jwt
        }
    }

    private suspend fun clearPreferences() {
        appContext.authPreferences.edit {
            it.clear()
        }
    }
}

private const val RESOURCE_VALUE_NAME = "com.thewebsnippet.service.jwt"
private const val DATASTORE_NAME = "authPreferences"
private val JWT = stringPreferencesKey("jwt")
private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
