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
package com.thewebsnippet.view.client

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebView
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.data.WebContent
import com.thewebsnippet.view.util.hasPermissionInManifest

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
internal open class TWSWebChromeClient(
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
                callback?.invoke(origin, isGranted, true)

                if (!isGranted) {
                    // clear permission state, so the user can request permission again
                    // https://stackoverflow.com/questions/53090545/why-is-ongeolocationpermissionsshowprompt-being-called-continuously
                    Handler(Looper.getMainLooper()).postDelayed({
                        GeolocationPermissions.getInstance().clear(origin)
                    }, CLEAR_PERMISSION_DELAY_MS)
                }
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

private const val CLEAR_PERMISSION_DELAY_MS = 500L
