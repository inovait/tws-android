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

import com.thewebsnippet.manager.domain.model.SnippetUpdateAction
import com.thewebsnippet.manager.domain.websocket.TWSSocket
import kotlinx.coroutines.flow.MutableSharedFlow

internal class FakeTWSSocket : TWSSocket {
    override var updateActionFlow: MutableSharedFlow<SnippetUpdateAction> = MutableSharedFlow()

    var isConnectionOpen = false

    override fun closeWebsocketConnection(): Boolean {
        isConnectionOpen = false
        return true
    }

    override fun setupWebSocketConnection(setupWssUrl: String, unauthorizedCallback: suspend () -> Unit) {
        isConnectionOpen = true
    }

    suspend fun mockUpdateAction(action: SnippetUpdateAction) {
        updateActionFlow.emit(action)
    }
}
