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

import android.content.Intent
import com.thewebsnippet.manager.data.notification.NotificationHandlerImpl
import com.thewebsnippet.manager.domain.model.SnippetNotificationBody
import com.thewebsnippet.manager.fakes.FakeNotificationDisplay
import com.thewebsnippet.manager.fakes.FakeNotificationPayloadParser
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class NotificationHandlerImplTest {
    private lateinit var fakeParser: FakeNotificationPayloadParser
    private lateinit var fakeDisplayer: FakeNotificationDisplay
    private lateinit var handler: NotificationHandlerImpl

    @Before
    fun setUp() {
        fakeParser = FakeNotificationPayloadParser()
        fakeDisplayer = FakeNotificationDisplay()
        handler = NotificationHandlerImpl(
            context = mock(),
            parser = fakeParser,
            displayer = fakeDisplayer
        )
    }

    @Test
    fun `handle returns false if parser returns null`() {
        fakeParser.nextResultBody = null

        val result = handler.handle(mapOf("foo" to "bar"))

        assertFalse(result)
    }

    @Test
    fun `handle returns true and calls displayer if parser returns payload`() {
        val payload = SnippetNotificationBody("project", "snippet", "title", "message")
        fakeParser.nextResultBody = payload

        val history = listOf(Intent("ACTION_1"))
        val result = handler.handle(mapOf("foo" to "bar"), history)

        assertTrue(result)
        assertEquals(payload, fakeDisplayer.lastPayload)
        assertEquals(history, fakeDisplayer.lastHistoryIntents)
    }
}
