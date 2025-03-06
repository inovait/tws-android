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
package com.thewebsnippet.manager.fakes.function

import com.thewebsnippet.manager.data.AccessTokenDto
import com.thewebsnippet.manager.data.RefreshTokenDto
import com.thewebsnippet.manager.function.TWSAuthFunction
import com.thewebsnippet.manager.utils.FakeService
import com.thewebsnippet.manager.utils.ServiceTestingHelper

internal class FakeTWSAuthFunction(
    private val helper: ServiceTestingHelper = ServiceTestingHelper()
) : TWSAuthFunction, FakeService by helper {
    var refreshToken: RefreshTokenDto? = null
    var accessToken: AccessTokenDto? = null

    override suspend fun register(): RefreshTokenDto {
        helper.intercept()

        return refreshToken ?: error("Returned refreshToken not faked!")
    }

    override suspend fun login(): AccessTokenDto {
        helper.intercept()

        return accessToken ?: error("Returned accessToken not faked!")
    }
}
