/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thewebsnippet.view.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.thewebsnippet.view.R
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
