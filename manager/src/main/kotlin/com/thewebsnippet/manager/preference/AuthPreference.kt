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

import android.content.Context
import kotlinx.coroutines.flow.Flow

internal interface AuthPreference {
    fun safeInit(context: Context)

    val jwt: String
    val refreshToken: Flow<String>
    val accessToken: Flow<String>

    suspend fun setRefreshToken(refreshToken: String)
    suspend fun setAccessToken(accessToken: String)
}
