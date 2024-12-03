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

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.thewebsnippet.core.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A JavaScript interface for handling file downloads in a WebView.
 * This class provides functionality to convert Base64 strings to files, store them,
 * and notify the user via the Android notification system or Toast messages.
 */
internal class JavaScriptDownloadInterface(private val context: Context) {

    @Suppress("Unused") // it is used in javascript
    @JavascriptInterface
    @Throws(IOException::class)
    fun convertBase64StringToFileAndStoreIt(uuid: String, base64File: String, mimeType: String, isGranted: Boolean) {
        val fileType = mimeType.substringAfterLast("/")

        val notificationId = NOTIFICATION_ID
        val downloadFileName = "$uuid.$fileType"
        val downloadPath = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString() + "/" + downloadFileName
        )

        val fileAsBytes = Base64.decode(base64File.replaceFirst("data:$mimeType;base64,", ""), 0)
        val os = FileOutputStream(downloadPath, false)
        os.write(fileAsBytes)
        os.flush()

        if (downloadPath.exists()) {
            val intent = Intent().apply {
                setAction(Intent.ACTION_VIEW)
                val apkURI = FileProvider.getUriForFile(
                    context, context.applicationContext.packageName + ".provider", downloadPath
                )
                setDataAndType(apkURI, MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileType))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            notificationManager?.notify(
                notificationId,
                notificationBuilder(context, notificationManager, downloadFileName, fileAsBytes, intent)
            )
        }
        if (!isGranted) {
            Toast.makeText(context, R.string.download_completed, Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("Unused") // it is used in javascript
    @JavascriptInterface
    fun loadStart() {
        Toast.makeText(context, R.string.downloading_file, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun getBase64StringFromBlobUrl(blobUrl: String, mimeType: String, notificationPermissionGranted: Boolean): String {
            val uuid = blobUrl.substringAfterLast("/")

            return "javascript: var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '$blobUrl', true);" +
                "xhr.setRequestHeader('Content-type','$mimeType');" +
                "xhr.responseType = 'blob';" +
                "xhr.loadstart = function(e) {" +
                "   $JAVASCRIPT_INTERFACE_NAME.loadStart();" +
                "};" +
                "xhr.onload = function(e) {" +
                "    if (this.status == 200) {" +
                "        var blobResponse = this.response;" +
                "        var reader = new FileReader();" +
                "        reader.readAsDataURL(blobResponse);" +
                "        reader.onloadend = function() {" +
                "            base64data = reader.result;" +
                "            $JAVASCRIPT_INTERFACE_NAME.convertBase64StringToFileAndStoreIt(" +
                "               '$uuid', " +
                "               base64data, " +
                "               '$mimeType', " +
                "               $notificationPermissionGranted" +
                "           );" +
                "        }" +
                "    }" +
                "};" +
                "xhr.send();"
        }

        const val JAVASCRIPT_INTERFACE_NAME = "Android"
        const val NOTIFICATION_ID = 999
    }
}
