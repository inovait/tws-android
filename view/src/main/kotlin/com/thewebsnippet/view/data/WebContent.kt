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
import com.thewebsnippet.data.TWSSnippet

/**
 * Represents different types of content that can be loaded in a WebView.
 *
 * NOTE: This class is a modified version of the original Accompanist WebView content class.
 * Modifications made include:
 * - Addition of `MessageOnly` type to handle custom web content loading from a pop up.
 */
sealed class WebContent {
    data class Snippet(val target: TWSSnippet) : WebContent()

    /**
     * Represents a state where the WebView is controlled only via a navigator,
     * without loading content directly into it. All communication with WebView must be done
     * through navigator.
     */
    data class NavigatorOnly(val default: TWSSnippet? = null) : WebContent()

    /**
     * Represents a popup message request for creating a new window in the WebView.
     *
     * @param msg The message containing the WebView's popup request details.
     * @param isDialog Indicates whether the content is a dialog.
     */
    internal data class MessageOnly(val msg: Message, val isDialog: Boolean) : WebContent()
}

internal fun WebContent.getSnippet() = when (this) {
    is WebContent.Snippet -> target
    is WebContent.NavigatorOnly -> default
    is WebContent.MessageOnly -> null
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
