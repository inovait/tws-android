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

package si.inova.tws.manager.preference

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@SuppressLint("DiscouragedApi") // RESOURCE_VALUE_NAME can only be retrieved from build
internal object AuthPreference {
    private lateinit var appContext: Context

    private val Context.authPreferences: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

    fun safeInit(context: Context) {
        if (::appContext.isInitialized) return

        appContext = context
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
}

private const val RESOURCE_VALUE_NAME = "si.inova.tws.service.jwt"
private const val DATASTORE_NAME = "authPreferences"
private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
