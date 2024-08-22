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

package si.inova.tws.core.data.view.client

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Message
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import si.inova.tws.core.data.view.LoadingState
import si.inova.tws.core.data.view.WebContent
import si.inova.tws.core.data.view.WebViewState
import si.inova.tws.core.util.hasPermissionInManifest

/**
 * TwsWebChromeClient, copied, modified and extended version of AccompanistWebChromeClient
 *
 * A parent class implementation of WebChromeClient that can be subclassed to add custom behaviour.
 *
 */

open class TwsWebChromeClient(
    private val popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null,
) : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set

    private lateinit var permissionRequest: (String, (Boolean) -> Unit) -> Unit

    override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
        popupStateCallback?.invoke(
            WebViewState(WebContent.MessageOnly).apply {
                popupMessage = resultMsg
            },
            true
        )

        return popupStateCallback != null
    }

    override fun onCloseWindow(window: WebView?) {
        popupStateCallback?.invoke(state, false)
        window?.destroy()
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished) return
        @Suppress("MagicNumber")
        state.loadingState = LoadingState.Loading(newProgress / 100.0f)
    }

    /**
     * Fix for:
     * Spotify have some protected content
     * https://stackoverflow.com/questions/53143363/how-to-enable-protected-content-in-a-webview
     */
    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.let {
            when {
                it.resources.contains(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID) -> it.grant(it.resources)
                it.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) ->
                    it.handleCameraPermission(state.webView?.context)
            }
        }
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
        super.onGeolocationPermissionsShowPrompt(origin, callback)
        val context = state.webView?.context

        if (context?.hasPermissionInManifest(Manifest.permission.ACCESS_COARSE_LOCATION) == true) {
            permissionRequest(Manifest.permission.ACCESS_COARSE_LOCATION) { isGranted ->
                callback?.invoke(origin, isGranted, false)
            }
        } else {
            callback?.invoke(origin, false, false)
        }
    }

    fun setupPermissionRequestCallback(callback: (String, (Boolean) -> Unit) -> Unit) {
        permissionRequest = callback
    }

    private fun PermissionRequest.handleCameraPermission(context: Context?) {
        if (context?.hasPermissionInManifest(Manifest.permission.CAMERA) == true) {
            permissionRequest(Manifest.permission.CAMERA) { isGranted ->
                if (isGranted) {
                    grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                } else {
                    deny()
                }
            }
        } else {
            deny()
        }
    }
}

