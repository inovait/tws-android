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

package com.thewebsnippet.manager

import com.thewebsnippet.manager.domain.model.AccessTokenDto
import com.thewebsnippet.manager.fakes.function.FakeTWSAuthFunction
import com.thewebsnippet.manager.fakes.preference.FakeAuthPreference
import com.thewebsnippet.manager.data.auth.AuthLoginManagerImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AuthLoginManagerImplTest {
    private val fakePreference = FakeAuthPreference()
    private val fakeAuthFunctions = FakeTWSAuthFunction()

    private lateinit var authLoginManagerImpl: AuthLoginManagerImpl

    @Before
    fun setup() {
        authLoginManagerImpl = AuthLoginManagerImpl(fakePreference, fakeAuthFunctions)

        val testToken = "token_123"
        fakeAuthFunctions.accessToken = AccessTokenDto(accessToken = testToken)
    }

    @Test
    fun `refreshToken called twice should only call setAccessToken once`() = runTest {
        val refresh1 = launch { authLoginManagerImpl.refreshToken() }
        val refresh2 = launch { authLoginManagerImpl.refreshToken() }

        refresh1.join()
        refresh2.join()

        assert(fakeAuthFunctions.loginCalled == 1)
    }
}
