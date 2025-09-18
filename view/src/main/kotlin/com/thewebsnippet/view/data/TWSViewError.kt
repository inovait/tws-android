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
import okhttp3.Request

/**
 * A wrapper class to hold errors from the TWSView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView error handling class.
 *
 * This class helps track errors that occur during the loading of web resources within the WebView.
 */

sealed class TWSViewError {
    class ResourceError(val error: WebResourceError, val request: WebResourceRequest?) : TWSViewError()

    class InitialLoadError(val error: Exception, val request: Request?) : TWSViewError()
}
