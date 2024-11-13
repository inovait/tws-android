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

package si.inova.tws.core.client

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Message
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebView
import si.inova.tws.core.data.WebContent
import si.inova.tws.core.data.TWSViewState
import si.inova.tws.core.util.hasPermissionInManifest

/**
 * TwsWebChromeClient is a modified and extended version of [AccompanistWebChromeClient].
 *
 * This class serves as a parent implementation of [AccompanistWebChromeClient], allowing customization
 * and extension for specific behaviors in handling web content in a [WebView].
 *
 * It includes features for managing permission requests, file chooser dialogs, and handling
 * new window creation events.
 *
 * @param popupStateCallback An optional callback that provides updates regarding the
 * state of popup windows within the WebView.
 */
open class TWSWebChromeClient(
    private val popupStateCallback: ((TWSViewState, Boolean) -> Unit)? = null,
) : AccompanistWebChromeClient() {
    private lateinit var showPermissionRequest: (String, (Boolean) -> Unit) -> Unit
    private lateinit var showFileChooser: (ValueCallback<Array<Uri>>, FileChooserParams) -> Unit

    fun setupPermissionRequestCallback(callback: (String, (Boolean) -> Unit) -> Unit) {
        showPermissionRequest = callback
    }

    fun setupFileChooserRequestCallback(callback: (ValueCallback<Array<Uri>>, FileChooserParams) -> Unit) {
        showFileChooser = callback
    }

    override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
        popupStateCallback?.invoke(
            TWSViewState(WebContent.MessageOnly(msg = resultMsg, isDialog = isDialog)),
            true
        )

        return popupStateCallback != null
    }

    override fun onCloseWindow(window: WebView?) {
        popupStateCallback?.invoke(state, false)
        window?.destroy()
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
        showFileChooser(filePathCallback, fileChooserParams)
        return true
    }

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
            showPermissionRequest(Manifest.permission.ACCESS_COARSE_LOCATION) { isGranted ->
                callback?.invoke(origin, isGranted, false)
            }
        } else {
            callback?.invoke(origin, false, false)
        }
    }

    private fun PermissionRequest.handleCameraPermission(context: Context?) {
        if (context == null) {
            deny()
            return
        }

        if (context.hasPermissionInManifest(Manifest.permission.CAMERA)) {
            showPermissionRequest(Manifest.permission.CAMERA) { isGranted ->
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
