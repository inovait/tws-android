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
package com.thewebsnippet.manager.manager.auth

import com.thewebsnippet.manager.factory.BaseServiceFactory
import com.thewebsnippet.manager.factory.create
import com.thewebsnippet.manager.function.TWSAuthFunction
import com.thewebsnippet.manager.preference.AuthPreference
import kotlinx.coroutines.flow.Flow

internal class AuthRegisterManagerImpl(
    private val twsAuth: TWSAuthFunction = BaseServiceFactory().create()
) : Auth {
    override val getToken: Flow<String>
        get() = AuthPreference.refreshToken

    override suspend fun refreshToken() {
        val response = twsAuth.register()
        AuthPreference.setRefreshToken(response.refreshToken)
    }
}
