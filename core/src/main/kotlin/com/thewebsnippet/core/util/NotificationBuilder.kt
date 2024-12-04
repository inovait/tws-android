/*
 * Copyright 2024 INOVA IT d.o.o.
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

package com.thewebsnippet.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.thewebsnippet.core.R
import kotlin.math.log10
import kotlin.math.pow

internal fun notificationBuilder(
    context: Context,
    notificationManager: NotificationManager,
    downloadFileName: String,
    fileAsBytes: ByteArray,
    intent: Intent
): Notification {
    val pendingIntent = PendingIntent.getActivity(
        context,
        1,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_DOWNLOAD_CHANNEL_ID,
            NOTIFICATION_DOWNLOAD_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        return Notification.Builder(context, NOTIFICATION_DOWNLOAD_CHANNEL_ID)
            .setContentTitle(downloadFileName)
            .setContentText(context.getString(R.string.download_completed_size, fileAsBytes.toHumanReadableSize(context)))
            .setContentIntent(pendingIntent)
            .setChannelId(NOTIFICATION_DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .build()
    } else {
        return NotificationCompat.Builder(context, NOTIFICATION_DOWNLOAD_CHANNEL_ID)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(downloadFileName)
            .setContentIntent(pendingIntent)
            .setContentText(context.getString(R.string.download_completed_size, fileAsBytes.toHumanReadableSize(context))).build()
    }
}

private fun ByteArray.toHumanReadableSize(context: Context): String {
    val size = this.size.toDouble()
    if (size <= 0) return context.getString(R.string.file_size_default_byte)

    val units = arrayOf(
        context.getString(R.string.file_size_byte),
        context.getString(R.string.file_size_kilo_byte),
        context.getString(R.string.file_size_mega_byte),
        context.getString(R.string.file_size_giga_byte),
        context.getString(R.string.file_size_tera_byte)
    )
    val digitGroups = (log10(size) / log10(BINARY_PREFIX)).toInt()

    return context.getString(R.string.human_readable_size, size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

private const val NOTIFICATION_DOWNLOAD_CHANNEL_ID = "DOWNLOAD_CHANNEL"
private const val NOTIFICATION_DOWNLOAD_NAME = "DOWNLOAD NOTIFICATION"
private const val BINARY_PREFIX = 1024.0
