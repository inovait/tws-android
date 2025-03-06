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
package com.thewebsnippet.view.data

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebView
import com.thewebsnippet.view.util.JavaScriptDownloadInterface
import com.thewebsnippet.view.util.hasPermissionInManifest

/**
 * TWSDownloadListener handles file downloads from a WebView, supporting both blob URLs and standard URLs.

 * Uses JavaScript for blob downloads.
 * Uses DownloadManager for regular downloads, with permission handling for notifications
 */
internal class TWSDownloadListener(
    private val context: Context,
    private val webView: WebView,
    private val permissionRequest: (String, (Boolean) -> Unit) -> Unit
) : DownloadListener {

    override fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    ) {
        if (url.startsWith(BLOB_URL)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.hasPermissionInManifest(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                permissionRequest(Manifest.permission.POST_NOTIFICATIONS) { isGranted ->
                    webView.evaluateJavascript(
                        JavaScriptDownloadInterface.getBase64StringFromBlobUrl(url, mimetype, isGranted), null
                    )
                }
            } else {
                webView.evaluateJavascript(
                    JavaScriptDownloadInterface.getBase64StringFromBlobUrl(url, mimetype, true), null
                )
            }
        } else {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimetype)
                addRequestHeader(USER_AGENT, userAgent)
                setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, contentDisposition, mimetype)
                )
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager?
            downloadManager?.enqueue(request)
        }
    }

    companion object {
        private const val BLOB_URL = "blob"
        private const val USER_AGENT = "User-Agent"
    }
}
