/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.fakes.preference

import com.thewebsnippet.manager.domain.preference.AuthPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeAuthPreference : AuthPreference {
    private val _refreshToken: MutableStateFlow<String?> = MutableStateFlow(null)
    override val refreshToken: Flow<String> = _refreshToken.filterNotNull()

    private val _accessToken: MutableStateFlow<String?> = MutableStateFlow(null)
    override val accessToken: Flow<String> = _accessToken.filterNotNull()

    override suspend fun setRefreshToken(refreshToken: String) {
        _refreshToken.value = refreshToken
    }

    override suspend fun setAccessToken(accessToken: String) {
        _accessToken.value = accessToken
    }
}
