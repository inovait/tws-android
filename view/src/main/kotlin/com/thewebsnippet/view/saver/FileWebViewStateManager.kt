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

package com.thewebsnippet.view.saver

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.webkit.WebView
import java.io.File

internal class FileWebViewStateManager : WebViewStateManager {
    override fun saveWebViewState(context: Context, webView: WebView, key: String): String? {
        return try {
            val bundle = Bundle()
            webView.saveState(bundle)
            val parcel = Parcel.obtain()
            bundle.writeToParcel(parcel, 0)
            val bytes = parcel.marshall()
            parcel.recycle()
            val file = File(context.cacheDir, "webview_state_$key.bin")
            file.outputStream().use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("FileWebViewStateManager", "Error saving webview state", e)
            null
        }
    }

    override fun restoreWebViewState(webView: WebView, path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) return false
            val bytes = file.readBytes()
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val bundle = Bundle.CREATOR.createFromParcel(parcel)
            parcel.recycle()
            webView.restoreState(bundle) != null
        } catch (e: Exception) {
            Log.e("FileWebViewStateManager", "Error restoring webview state", e)
            false
        }
    }

    override fun deleteWebViewState(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {
            Log.e("FileWebViewStateManager", "Error deleting webview state")
        }
    }
}
