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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.thewebsnippet.manager.R
import com.thewebsnippet.manager.domain.model.SnippetNotificationBody
import com.thewebsnippet.manager.domain.notification.NotificationDisplay
import com.thewebsnippet.manager.ui.TWSViewPopupActivity

internal class NotificationDisplayImpl(
    private val context: Context,
    private val channelId: String = CHANNEL_ID,
    private val channelName: String = CHANNEL_NAME
) : NotificationDisplay {

    override fun display(
        payload: SnippetNotificationBody,
        historyIntents: List<Intent>
    ): Boolean {
        val pendingIntent = buildPendingIntent(
            context,
            payload.projectId,
            payload.snippetId,
            historyIntents
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_default_notification)
            .setContentTitle(payload.title)
            .setContentText(payload.message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        createChannelIfNeeded(manager)

        manager.notify(NOTIFICATION_ID, notification)

        return true
    }

    private fun buildPendingIntent(
        context: Context,
        projectId: String,
        snippetId: String,
        historyIntents: List<Intent>
    ): PendingIntent {
        val detailIntent = TWSViewPopupActivity.createIntent(context, snippetId, projectId)

        val stackBuilder = TaskStackBuilder.create(context).apply {
            historyIntents.forEach { addNextIntent(it) }
            addNextIntent(detailIntent)
        }

        return stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ) ?: error("Should not occur")
    }

    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}

private const val NOTIFICATION_ID = 1001
private const val CHANNEL_ID = "twsChannel"
private const val CHANNEL_NAME = "TWS Channel"
