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

import android.webkit.WebResourceError
import android.webkit.WebViewClient.ERROR_AUTHENTICATION
import android.webkit.WebViewClient.ERROR_BAD_URL
import android.webkit.WebViewClient.ERROR_CONNECT
import android.webkit.WebViewClient.ERROR_FAILED_SSL_HANDSHAKE
import android.webkit.WebViewClient.ERROR_FILE
import android.webkit.WebViewClient.ERROR_FILE_NOT_FOUND
import android.webkit.WebViewClient.ERROR_HOST_LOOKUP
import android.webkit.WebViewClient.ERROR_IO
import android.webkit.WebViewClient.ERROR_TIMEOUT
import android.webkit.WebViewClient.ERROR_TOO_MANY_REQUESTS
import android.webkit.WebViewClient.ERROR_UNSAFE_RESOURCE
import android.webkit.WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME
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
internal fun WebResourceError.getUserFriendlyMessage(): String? {
    return when (errorCode) {
        ERROR_HOST_LOOKUP,
        ERROR_CONNECT,
        ERROR_TIMEOUT -> stringResource(id = R.string.error_no_network)

        ERROR_UNSUPPORTED_AUTH_SCHEME,
        ERROR_AUTHENTICATION -> stringResource(id = R.string.error_authentication)

        ERROR_BAD_URL -> stringResource(id = R.string.error_invalid_url)

        ERROR_FILE_NOT_FOUND -> stringResource(id = R.string.error_file_not_found)

        ERROR_UNSAFE_RESOURCE -> stringResource(id = R.string.error_unsafe_resource)

        ERROR_FAILED_SSL_HANDSHAKE -> stringResource(id = R.string.error_ssl_handshake)

        ERROR_TOO_MANY_REQUESTS -> stringResource(id = R.string.error_too_many_requests)

        ERROR_FILE,
        ERROR_IO -> stringResource(id = R.string.error_generic_io)

        else -> null
    }
}
