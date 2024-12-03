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

package com.thewebsnippet.manager.manager.auth

import com.thewebsnippet.manager.factory.BaseServiceFactory
import com.thewebsnippet.manager.factory.create
import com.thewebsnippet.manager.function.TWSAuthFunction
import com.thewebsnippet.manager.preference.AuthPreference
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
internal class AuthLoginManagerImpl(
    private val authPreference: AuthPreference,
    snippetRegister: Auth = AuthRegisterManagerImpl(authPreference),
    private val twsAuth: TWSAuthFunction = BaseServiceFactory(snippetRegister).create()
) : Auth {
    override val getToken: Flow<String>
        get() = authPreference.authToken

    override suspend fun refreshToken() {
        val response = twsAuth.login()
        authPreference.setAuthToken(response.authToken)
    }
}
