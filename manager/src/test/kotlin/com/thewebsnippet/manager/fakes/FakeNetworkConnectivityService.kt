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
package com.thewebsnippet.manager.fakes

import com.thewebsnippet.manager.domain.model.NetworkStatus
import com.thewebsnippet.manager.domain.connectivity.NetworkConnectivityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeNetworkConnectivityService : NetworkConnectivityService {
    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Connected)
    override val networkStatus: Flow<NetworkStatus>
        get() = _networkStatus

    override val isConnected: Boolean
        get() = _networkStatus.value == NetworkStatus.Connected

    fun mockNetworkStatus(status: NetworkStatus) {
        _networkStatus.value = status
    }
}
