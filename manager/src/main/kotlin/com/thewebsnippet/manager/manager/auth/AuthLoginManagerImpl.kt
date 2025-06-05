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
import kotlinx.coroutines.sync.Mutex

internal class AuthLoginManagerImpl(
    private val auth: AuthPreference,
    private val twsAuth: TWSAuthFunction = BaseServiceFactory(AuthRegisterManagerImpl(auth)).create()
) : Auth {
    override val getToken: Flow<String>
        get() = auth.accessToken

    private val mutex = Mutex()

    override suspend fun refreshToken() {
        if (mutex.isLocked) return

        mutex.lock()
        try {
            val response = twsAuth.login()
            auth.setAccessToken(response.accessToken)
        } finally {
            mutex.unlock()
        }
    }
}
