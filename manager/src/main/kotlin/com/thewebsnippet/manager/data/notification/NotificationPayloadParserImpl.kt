/*
 * Copyright 2025 INOVA IT d.o.o.
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

package com.thewebsnippet.manager.data.notification

import com.thewebsnippet.manager.domain.model.SnippetNotificationBody
import com.thewebsnippet.manager.domain.model.SnippetNotificationMetadata
import com.thewebsnippet.manager.domain.notification.NotificationPayloadParser

internal class NotificationPayloadParserImpl(
    private val supportedTypes: Set<String> = setOf(SUPPORTED_TYPE_SNIPPET_PUSH)
) : NotificationPayloadParser {

    override fun parseNotification(data: Map<String, String>): SnippetNotificationBody? {
        val type = data[PAYLOAD_TYPE]
        if (type !in supportedTypes) return null

        val path = data[PAYLOAD_SNIPPET_PATH] ?: return null
        val title = data[PAYLOAD_MESSAGE_TITLE]
        val message = data[PAYLOAD_MESSAGE_CONTENT]

        val (snippetId, projectId) = path.split("/")
        val snippetMetadataAvailable = projectId.isNotBlank() && snippetId.isNotBlank()

        return if (
            snippetMetadataAvailable &&
            !title.isNullOrBlank() &&
            !message.isNullOrBlank()
        ) {
            SnippetNotificationBody(projectId, snippetId, title, message)
        } else {
            null
        }
    }

    override fun parseMetadata(data: Map<String, String>): SnippetNotificationMetadata? {
        val type = data[PAYLOAD_TYPE]
        if (type !in supportedTypes) return null

        val path = data[PAYLOAD_SNIPPET_PATH] ?: return null
        val (snippetId, projectId) = path.split("/")

        return if (projectId.isNotBlank() && snippetId.isNotBlank()) {
            SnippetNotificationMetadata(projectId, snippetId)
        } else {
            null
        }
    }

    companion object {
        private const val PAYLOAD_TYPE = "type"
        private const val PAYLOAD_SNIPPET_PATH = "path"
        private const val PAYLOAD_MESSAGE_TITLE = "title"
        private const val PAYLOAD_MESSAGE_CONTENT = "message"

        private const val SUPPORTED_TYPE_SNIPPET_PUSH = "snippet_push"
    }
}
