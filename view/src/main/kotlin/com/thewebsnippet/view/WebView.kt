/*
 * Copyright 2021 The Android Open Source Project
 * Modifications Copyright 2024 INOVA IT d.o.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file has been modified from its original version.
 */
package com.thewebsnippet.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thewebsnippet.view.client.AccompanistWebChromeClient
import com.thewebsnippet.view.client.AccompanistWebViewClient
import com.thewebsnippet.view.client.TWSWebChromeClient
import com.thewebsnippet.view.data.TWSDownloadListener
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewNavigator
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.data.WebContent
import com.thewebsnippet.view.data.rememberTWSViewNavigator
import com.thewebsnippet.view.util.JavaScriptDownloadInterface
import com.thewebsnippet.view.util.JavaScriptDownloadInterface.Companion.JAVASCRIPT_INTERFACE_NAME

/**
 *  A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 *
 * NOTE: This is a modified copy from Accompanist's WebView wrapper, since it is no longer supported.
 * The original implementation can be found at https://google.github.io/accompanist/web/.
 * This modified version allows further customization of the component according to our needs.
 * -----
 * Modifications include:
 * - Added `isRefreshable` option for pull-to-refresh functionality.
 * - Enhanced lifecycle management using `LifecycleResumeEffect`.
 * - Added permission handling for file downloads.
 * - Added JavaScript interface for download operations.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param isRefreshable An option to have pull to refresh
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing.
 * @param chromeClient Provides access to WebChromeClient via subclassing.
 */
@Composable
internal fun WebView(
    state: TWSViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    isRefreshable: Boolean = true,
    navigator: TWSViewNavigator = rememberTWSViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() }
) {
    BoxWithConstraints(modifier) {
        // WebView changes it's layout strategy based on
        // it's layoutParams. We convert from Compose Modifier to
        // layout params here.
        val width = if (constraints.hasFixedWidth) {
            LayoutParams.MATCH_PARENT
        } else {
            LayoutParams.WRAP_CONTENT
        }
        val height = if (constraints.hasFixedHeight) {
            LayoutParams.MATCH_PARENT
        } else {
            LayoutParams.WRAP_CONTENT
        }

        val layoutParams = FrameLayout.LayoutParams(width, height)

        WebView(
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            isRefreshable,
            navigator,
            onCreated,
            onDispose,
            client,
            chromeClient
        )
    }
}

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * This is a modified copy from Accompanists WebView wrapper, since it is not supported anymore and allows
 * us to further customize the component according to our needs. Check https://google.github.io/accompanist/web/
 * for default implementation.
 **
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param layoutParams layout information for WebView
 * @param modifier A compose modifier.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param isRefreshable An option to have pull to refresh
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing.
 * @param chromeClient Provides access to WebChromeClient via subclassing.
 */
@Composable
internal fun WebView(
    state: TWSViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    isRefreshable: Boolean = true,
    navigator: TWSViewNavigator = rememberTWSViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() }
) {
    val webView = state.webView

    HandleBackPresses(captureBackPresses, navigator, webView)
    webView?.let {
        WebViewResumeOrPauseEffect(it)
    }

    val (permissionLauncher, permissionCallback) = createPermissionLauncher()

    if (chromeClient is TWSWebChromeClient) {
        SetupFileChooserLauncher(chromeClient)
        SetupPermissionHandling(chromeClient, permissionLauncher) { callback ->
            permissionCallback.value = callback
        }
    }

    webView?.let { wv -> HandleNavigationEvents(wv, navigator, state) }

    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    AndroidView(
        factory = { context ->
            createSwipeRefreshLayout(
                context = context,
                navigator = navigator,
                state = state,
                webView = createWebView(
                    context = context,
                    state = state,
                    onCreated = { wv ->
                        onCreated(wv)
                        wv.layoutParams = layoutParams
                        wv.setDownloadListener(
                            TWSDownloadListener(context, wv) { permission, callback ->
                                permissionLauncher.launch(permission)
                                permissionCallback.value = callback
                            }
                        )
                    },
                    client = client,
                    chromeClient = chromeClient
                )
            )
        },
        modifier = modifier,
        onRelease = {
            val wv = state.webView ?: return@AndroidView

            state.viewState = Bundle().apply {
                wv.saveState(this)
            }.takeIf { bundle ->
                !bundle.isEmpty
            } ?: state.viewState
            state.webView = null

            onDispose(wv)
        },
        update = {
            if (isRefreshable) {
                it.isRefreshing = (state.loadingState as? TWSLoadingState.Loading)?.isUserForceRefresh == true
            }
        }
    )
}

@Composable
private fun HandleBackPresses(captureBackPresses: Boolean, navigator: TWSViewNavigator, webView: WebView?) {
    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }
}

@Composable
private fun HandleNavigationEvents(wv: WebView, navigator: TWSViewNavigator, state: TWSViewState) {
    LaunchedEffect(wv, navigator) {
        with(navigator) {
            wv.handleNavigationEvents()
        }
    }

    LaunchedEffect(wv, state) {
        snapshotFlow { state.content }.collect { content ->
            when (content) {
                is WebContent.Url -> {
                    wv.loadUrl(content.url, content.additionalHttpHeaders)
                }

                is WebContent.Data -> {
                    wv.loadDataWithBaseURL(
                        content.baseUrl,
                        content.data,
                        content.mimeType,
                        content.encoding,
                        content.historyUrl
                    )
                }

                is WebContent.NavigatorOnly, is WebContent.MessageOnly -> {
                    // NO-OP
                }
            }
        }
    }
}

@Composable
private fun WebViewResumeOrPauseEffect(webView: WebView) {
    LifecycleResumeEffect(Unit) {
        webView.onResume()

        onPauseOrDispose {
            webView.onPause()
        }
    }
}

@Composable
private fun SetupPermissionHandling(
    chromeClient: TWSWebChromeClient,
    permissionLauncher: ActivityResultLauncher<String>,
    setupCallback: ((Boolean) -> Unit) -> Unit
) {
    LaunchedEffect(chromeClient) {
        chromeClient.setupPermissionRequestCallback { permission, callback ->
            permissionLauncher.launch(permission)
            setupCallback(callback)
        }
    }
}

@Composable
private fun createPermissionLauncher(): Pair<ActivityResultLauncher<String>, MutableState<((Boolean) -> Unit)?>> {
    val permissionCallback = remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionCallback.value?.invoke(isGranted)
    }
    return permissionLauncher to permissionCallback
}

@Composable
private fun SetupFileChooserLauncher(chromeClient: TWSWebChromeClient) {
    val context = LocalContext.current

    val fileChooserCallback = remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val results = result.data?.data?.let { arrayOf(it) } ?: result.data?.clipData?.let {
            (0 until it.itemCount).map { index -> it.getItemAt(index).uri }.toTypedArray()
        }
        fileChooserCallback.value?.onReceiveValue(results)
        fileChooserCallback.value = null
    }

    LaunchedEffect(chromeClient) {
        chromeClient.setupFileChooserRequestCallback { valueCallback, fileChooserParams ->
            fileChooserCallback.value = valueCallback
            fileChooserLauncher.launch(
                Intent.createChooser(
                    fileChooserParams.createIntent(),
                    context.getString(R.string.file_chooser_title)
                )
            )
        }
    }
}

private fun createWebView(
    context: Context,
    state: TWSViewState,
    onCreated: (WebView) -> Unit,
    client: WebViewClient,
    chromeClient: WebChromeClient
): WebView {
    return WebView(context).apply {
        webChromeClient = chromeClient
        webViewClient = client

        state.viewState?.let { this.restoreState(it) }
        onCreated(this)

        addJavascriptInterface(JavaScriptDownloadInterface(context), JAVASCRIPT_INTERFACE_NAME)
    }.also { state.webView = it }
}

private fun createSwipeRefreshLayout(
    context: Context,
    webView: WebView,
    navigator: TWSViewNavigator,
    state: TWSViewState
): SwipeRefreshLayout {
    return SwipeRefreshLayout(context).apply {
        setOnRefreshListener {
            state.loadingState = TWSLoadingState.ForceRefreshInitiated
            navigator.reload()
        }
        addView(webView)
    }
}
