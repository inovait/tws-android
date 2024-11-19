/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * This file contains modifications based on code from the Accompanist WebView library.
 * Original Copyright (c) 2021 The Android Open Source Project, licensed under the Apache License, Version 2.0.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice, this permission notice, and the following additional notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * -----
 * Portions of this file are derived from the Accompanist WebView library,
 * which is available at https://github.com/google/accompanist/blob/main/web/src/main/java/com/google/accompanist/web/WebView.kt.
 * Copyright (c) 2021 The Android Open Source Project. Licensed under Apache License, Version 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * -----
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.core.data

import android.os.Message
import android.webkit.WebView

/**
 * Represents different types of content that can be loaded in a WebView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView content class.
 * Modifications made include:
 * - Addition of `MessageOnly` type to handle custom web content loading from a pop up.
 */
sealed class WebContent {
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap()
    ) : WebContent()

    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null
    ) : WebContent()

    data object NavigatorOnly : WebContent()

    data class MessageOnly(val msg: Message, val isDialog: Boolean) : WebContent()
}

/**
 * Handles the creation of a new window for the WebView, when a message requesting
 * a new window is received.
 *
 * @param webView The WebView instance where the new window should be loaded in.
 */
internal fun WebContent.MessageOnly.onCreateWindowStatus(webView: WebView) {
    val transport = msg.obj as? WebView.WebViewTransport
    if (transport != null) {
        transport.webView = webView
        msg.sendToTarget()
    }
}
