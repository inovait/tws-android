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

package com.thewebsnippet.core.data

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebView
import com.thewebsnippet.core.util.JavaScriptDownloadInterface
import com.thewebsnippet.core.util.hasPermissionInManifest

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
