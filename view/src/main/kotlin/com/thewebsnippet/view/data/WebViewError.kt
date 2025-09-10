/*
 * Copyright 2021 The Android Open Source Project
 * Modifications Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified from its original version.
 */
package com.thewebsnippet.view.data

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient.ERROR_HOST_LOOKUP
import androidx.compose.runtime.Immutable

/**
 * A wrapper class to hold errors from the WebView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView error handling class.
 *
 * This class helps track errors that occur during the loading of web resources within the WebView.
 */
@Immutable
data class WebViewError(
    /**
     * The request the error came from.
     */
    val request: WebResourceRequest?,
    /**
     * The error that was reported.
     */
    val error: WebResourceError
)

internal fun WebViewError.isDisplayable() = request?.isForMainFrame == true || error.errorCode == ERROR_HOST_LOOKUP
