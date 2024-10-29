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

package si.inova.tws.manager.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import si.inova.tws.manager.data.SnippetUpdateAction
import si.inova.tws.manager.data.WebSocketStatus
import si.inova.tws.manager.websocket.TWSSocketListener

class FakeTWSSocketListener : TWSSocketListener() {
    private val _updateActionFlow: MutableStateFlow<SnippetUpdateAction?> = MutableStateFlow(null)
    override val updateActionFlow: Flow<SnippetUpdateAction> = _updateActionFlow.filterNotNull()

    private val _socketStatus: MutableStateFlow<WebSocketStatus?> = MutableStateFlow(null)
    override val socketStatus: Flow<WebSocketStatus> = _socketStatus.filterNotNull()

    fun mockUpdateAction(action: SnippetUpdateAction) {
        _updateActionFlow.update { action }
    }

    fun mockSocketStatus(action: WebSocketStatus) {
        _socketStatus.update { action }
    }
}
