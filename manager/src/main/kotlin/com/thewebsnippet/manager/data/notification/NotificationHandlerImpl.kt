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

import android.content.Context
import android.content.Intent
import com.thewebsnippet.manager.domain.notification.NotificationDisplay
import com.thewebsnippet.manager.domain.notification.NotificationPayloadParser
import com.thewebsnippet.manager.domain.notification.NotificationHandler

internal class NotificationHandlerImpl(
    context: Context,
    private val parser: NotificationPayloadParser = NotificationPayloadParserImpl(),
    private val displayer: NotificationDisplay = NotificationDisplayImpl(context)
) : NotificationHandler {

    override fun handle(
        contentTitle: String,
        contentText: String,
        payload: Map<String, String>,
        smallIcon: Int,
        autoCancel: Boolean,
        priority: Int,
        historyIntents: List<Intent>
    ): Boolean {
        val parsedPayload = parser.parseMetadata(payload) ?: return false

        return displayer.display(
            contentTitle,
            contentText,
            parsedPayload,
            smallIcon,
            autoCancel,
            priority,
            historyIntents
        )
    }
}
