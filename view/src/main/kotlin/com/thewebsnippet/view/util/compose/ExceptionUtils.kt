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
package com.thewebsnippet.view.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thewebsnippet.view.R
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Composable
internal fun Exception.getUserFriendlyMessage(): String? {
    return when (this) {
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException -> stringResource(id = R.string.error_no_network)

        else -> null
    }
}

@Composable
internal fun Exception.isRefreshable(): Boolean {
    return when (this) {
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException -> true

        else -> false
    }
}
