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

package com.thewebsnippet.manager

import android.util.Log
import com.thewebsnippet.manager.data.notification.NotificationPayloadParserImpl
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NotificationPayloadParserImplTest {
    private lateinit var parser: NotificationPayloadParserImpl

    @Before
    fun setUp() {
        parser = NotificationPayloadParserImpl()

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun `parseNotification returns SnippetNotificationBody for valid payload`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "snippet123/project456",
            "title" to "Title",
            "message" to "Message"
        )

        val result = parser.parseNotification(payload)

        assertNotNull(result)
        assertEquals("project456", result?.projectId)
        assertEquals("snippet123", result?.snippetId)
        assertEquals("Title", result?.title)
        assertEquals("Message", result?.message)
    }

    @Test
    fun `parseNotification returns null if type is not supported`() {
        val payload = mapOf(
            "type" to "other_type",
            "path" to "snippet123/project456",
            "title" to "Title",
            "message" to "Message"
        )

        val result = parser.parseNotification(payload)

        assertNull(result)
    }

    @Test
    fun `parseNotification returns null if path is missing`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "title" to "Title",
            "message" to "Message"
        )

        val result = parser.parseNotification(payload)

        assertNull(result)
    }

    @Test
    fun `parseNotification returns null if title is blank`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "snippet123/project456",
            "title" to "",
            "message" to "Message"
        )

        val result = parser.parseNotification(payload)

        assertNull(result)
    }

    @Test
    fun `parseNotification returns null if message is blank`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "snippet123/project456",
            "title" to "Title",
            "message" to ""
        )

        val result = parser.parseNotification(payload)

        assertNull(result)
    }

    @Test
    fun `parseNotification returns null if path is invalid`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "snippet123", // Not "snippet/project"
            "title" to "Title",
            "message" to "Message"
        )

        val result = parser.parseNotification(payload)
        assertNull(result)
    }

    @Test
    fun `parseMetadata returns SnippetNotificationMetadata for valid payload`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "snippetABC/projectDEF"
        )

        val result = parser.parseMetadata(payload)

        assertNotNull(result)
        assertEquals("projectDEF", result?.projectId)
        assertEquals("snippetABC", result?.snippetId)
    }

    @Test
    fun `parseMetadata returns null if type is not supported`() {
        val payload = mapOf(
            "type" to "other_type",
            "path" to "snippetABC/projectDEF"
        )

        val result = parser.parseMetadata(payload)

        assertNull(result)
    }

    @Test
    fun `parseMetadata returns null if path is missing`() {
        val payload = mapOf(
            "type" to "snippet_push"
        )

        val result = parser.parseMetadata(payload)

        assertNull(result)
    }

    @Test
    fun `parseMetadata returns null if path is invalid`() {
        val payload = mapOf(
            "type" to "snippet_push",
            "path" to "justonestring"
        )

        val result = parser.parseMetadata(payload)
        assertNull(result)
    }
}
