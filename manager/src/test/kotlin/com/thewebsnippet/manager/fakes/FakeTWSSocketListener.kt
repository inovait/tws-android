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

import com.thewebsnippet.manager.data.SnippetUpdateAction
import com.thewebsnippet.manager.data.WebSocketStatus
import com.thewebsnippet.manager.websocket.TWSSocketListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeTWSSocketListener : TWSSocketListener() {
    override val updateActionFlow: Flow<SnippetUpdateAction>
        get() = _updateActionFlow

    override val socketStatus: Flow<WebSocketStatus>
        get() = _socketStatus

    private val _updateActionFlow = MutableSharedFlow<SnippetUpdateAction>()
    private val _socketStatus = MutableStateFlow<WebSocketStatus>(WebSocketStatus.Closed)

    fun mockSocketStatus(status: WebSocketStatus) {
        _socketStatus.value = status
    }

    suspend fun mockUpdateActionFlow(action: SnippetUpdateAction) {
        _updateActionFlow.emit(action)
    }
}
