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

    /**
     * Represents a URL to be loaded in the WebView.
     *
     * @param url The URL to load.
     * @param additionalHttpHeaders Optional HTTP headers to include in the request.
     */
    data class Url(
        val url: String,
        val additionalHttpHeaders: Map<String, String> = emptyMap()
    ) : WebContent()

    /**
     * Represents raw HTML or other data content to be loaded in the WebView.
     *
     * @param data The HTML or data content to load.
     * @param baseUrl Optional base URL for resolving relative paths in the content.
     * @param encoding The character encoding for the content, defaults to "utf-8".
     * @param mimeType Optional MIME type for the content; defaults to "text/html" if null.
     * @param historyUrl Optional URL for the WebView's history management.
     */
    data class Data(
        val data: String,
        val baseUrl: String? = null,
        val encoding: String = "utf-8",
        val mimeType: String? = null,
        val historyUrl: String? = null
    ) : WebContent()

    /**
     * Represents a state where the WebView is controlled only via a navigator,
     * without loading content directly into it. All communication with WebView must be done
     * through navigator.
     */
    data object NavigatorOnly : WebContent()

    /**
     * Represents a popup message request for creating a new window in the WebView.
     *
     * @param msg The message containing the WebView's popup request details.
     * @param isDialog Indicates whether the content is a dialog.
     */
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
