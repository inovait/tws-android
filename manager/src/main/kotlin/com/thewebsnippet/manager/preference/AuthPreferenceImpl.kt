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

package com.thewebsnippet.manager.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal class AuthPreferenceImpl(private val authPreferences: DataStore<Preferences>) : AuthPreference {

    private val storedJwt: Flow<String> by lazy {
        authPreferences.data
            .map { preferences ->
                preferences[DATASTORE_JWT] ?: ""
            }
    }

    init {
        runBlocking {
            if (storedJwt.first() != JWT.token && storedJwt.first().isNotEmpty()) {
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
}

private val DATASTORE_JWT = stringPreferencesKey("jwt")
private val DATASTORE_REFRESH_TOKEN = stringPreferencesKey("refreshToken")
private val DATASTORE_ACCESS_TOKEN = stringPreferencesKey("accessToken")
