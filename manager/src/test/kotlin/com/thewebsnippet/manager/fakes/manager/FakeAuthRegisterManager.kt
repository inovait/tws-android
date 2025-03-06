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
package com.thewebsnippet.manager.fakes.manager

import com.thewebsnippet.manager.fakes.function.FakeTWSAuthFunction
import com.thewebsnippet.manager.manager.auth.Auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

internal class FakeAuthRegisterManager(
    private val fakeTWSAuthFunction: FakeTWSAuthFunction = FakeTWSAuthFunction()
) : Auth {
    private val _getToken: MutableStateFlow<String?> = MutableStateFlow(null)
    override val getToken: Flow<String> = _getToken.filterNotNull()

    override suspend fun refreshToken() {
        fakeTWSAuthFunction.register()
        _getToken.value = fakeTWSAuthFunction.refreshToken?.refreshToken
    }
}
