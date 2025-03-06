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

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.thewebsnippet.view.R
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
