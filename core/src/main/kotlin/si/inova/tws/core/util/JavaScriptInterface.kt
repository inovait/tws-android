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

package si.inova.tws.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.app.NotificationCompat
import si.inova.tws.core.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.log10
import kotlin.math.pow

class JavaScriptInterface(private val context: Context) {

    @Suppress("Unused") // it is used in javascript
    @JavascriptInterface
    @Throws(IOException::class)
    fun getBase64FromBlobData(uuid: String, base64Data: String, mimeType: String) {
        convertBase64StringToPdfAndStoreIt(uuid, base64Data, mimeType)
    }

    @Throws(IOException::class)
    private fun convertBase64StringToPdfAndStoreIt(uuid: String, base64PDf: String, mimeType: String) {
        val fileType = mimeType.substringAfterLast("/")

        val notificationId = 1
        val downloadFileName = "$uuid.$fileType"
        val downloadPath = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + downloadFileName
        )
        val pdfAsBytes = Base64.decode(base64PDf.replaceFirst("data:$mimeType;base64,", ""), 0)
        val os = FileOutputStream(downloadPath, false)
        os.write(pdfAsBytes)
        os.flush()

        if (downloadPath.exists()) {
            val intent = Intent()
            intent.setAction(Intent.ACTION_VIEW)
            val pendingIntent =
                PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_DOWNLOAD_CHANNEL_ID,
                    NOTIFICATION_DOWNLOAD_NAME,
                    NotificationManager.IMPORTANCE_LOW
                )
                val notification = Notification.Builder(context, NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setContentTitle(downloadFileName)
                    .setContentText(context.getString(R.string.download_completed, pdfAsBytes.toHumanReadableSize(context)))
                    .setContentIntent(pendingIntent)
                    .setChannelId(NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .build()
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel)
                    notificationManager.notify(notificationId, notification)
                }
            } else {
                val b = NotificationCompat.Builder(context, NOTIFICATION_DOWNLOAD_CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle(downloadFileName)
                    .setContentText(context.getString(R.string.download_completed, pdfAsBytes.toHumanReadableSize(context)))

                notificationManager?.notify(notificationId, b.build())
            }
        }
        Toast.makeText(context, R.string.downloading_file, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String): String {
            val uuid = blobUrl.substringAfterLast("/")

            return "javascript: var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '$blobUrl', true);" +
                "xhr.setRequestHeader('Content-type','$mimeType');" +
                "xhr.responseType = 'blob';" +
                "xhr.onload = function(e) {" +
                "    if (this.status == 200) {" +
                "        var blobResponse = this.response;" +
                "        var reader = new FileReader();" +
                "        reader.readAsDataURL(blobResponse);" +
                "        reader.onloadend = function() {" +
                "            base64data = reader.result;" +
                "            Android.getBase64FromBlobData('$uuid', base64data, '$mimeType');" +
                "        }" +
                "    }" +
                "};" +
                "xhr.send();"
        }

        private const val NOTIFICATION_DOWNLOAD_CHANNEL_ID = "DOWNLOAD_CHANNEL"
        private const val NOTIFICATION_DOWNLOAD_NAME = "DOWNLOAD NOTIFICATION"
    }
}

private fun ByteArray.toHumanReadableSize(context: Context): String {
    val size = this.size.toDouble()
    if (size <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size) / log10(1024.0)).toInt()


    return context.getString(R.string.human_readable_size, size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}
