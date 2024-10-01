/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.tws.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import si.inova.tws.core.client.AccompanistWebChromeClient
import si.inova.tws.core.client.AccompanistWebViewClient
import si.inova.tws.core.client.OkHttpTwsWebViewClient
import si.inova.tws.core.client.TwsWebChromeClient
import si.inova.tws.core.client.TwsWebViewClient
import si.inova.tws.core.data.ModifierPageData
import si.inova.tws.core.data.view.TwsDownloadListener
import si.inova.tws.core.data.view.WebContent
import si.inova.tws.core.data.view.WebViewNavigator
import si.inova.tws.core.data.view.WebViewState
import si.inova.tws.core.data.view.rememberWebViewNavigator
import si.inova.tws.core.util.JavaScriptDownloadInterface
import si.inova.tws.core.util.JavaScriptDownloadInterface.Companion.JAVASCRIPT_INTERFACE_NAME

/**
 * NOTE: This is a copy from Accompanists WebView wrapper, since it is not supported anymore and allows
 * us to further customize the component according to our needs. Check https://google.github.io/accompanist/web/
 * for default implementation or look at the git history of the file to see the customizations
 *
 *
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param key A property, which allows us to recreate webview when needed
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
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
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param interceptOverrideUrl optional callback, how to handle intercepted urls,
 * return true if do not want to navigate to the new url and return false if navigation to the new url is intact
 * @param factory An optional WebView factory for using a custom subclass of WebView
 * @param dynamicModifiers An optional parameter to set up a JS script.
 */
@Composable
fun WebView(
    key: Any?,
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    isRefreshable: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    interceptOverrideUrl: (String) -> Boolean = { false },
    factory: ((Context) -> WebView)? = null,
    dynamicModifiers: List<ModifierPageData>? = null
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
            key,
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            isRefreshable,
            navigator,
            onCreated,
            onDispose,
            client,
            chromeClient,
            interceptOverrideUrl,
            factory,
            dynamicModifiers
        )
    }
}

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param key A property, which allows us to recreate webview when needed
 * @param state The webview state holder where the Uri to load is defined.
 * @param layoutParams A FrameLayout.LayoutParams object to custom size the underlying WebView.
 * @param modifier A compose modifier
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
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param interceptOverrideUrl optional callback, how to handle intercepted urls,
 * return true if do not want to navigate to the new url and return false if navigation to the new url is intact
 * @param factory An optional WebView factory for using a custom subclass of WebView
 * @param dynamicModifiers An optional parameter to inject a JS.
 */
@Composable
fun WebView(
    key: Any?,
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    isRefreshable: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    interceptOverrideUrl: (String) -> Boolean = { false },
    factory: ((Context) -> WebView)? = null,
    dynamicModifiers: List<ModifierPageData>? = null
) {
    val webView = state.webView

    HandleBackPresses(captureBackPresses, navigator, webView)
    WebViewResumeOrPauseEffect(webView)

    val (permissionLauncher, permissionCallback) = createPermissionLauncher()

    if (chromeClient is TwsWebChromeClient) {
        SetupFileChooserLauncher(chromeClient)
        SetupPermissionHandling(chromeClient, permissionLauncher) { callback ->
            permissionCallback.value = callback
        }
    }


    webView?.let { wv -> HandleNavigationEvents(wv, navigator, state) }

    client.apply {
        this.state = state
        this.navigator = navigator

        (this as? TwsWebViewClient)?.interceptOverrideUrl = interceptOverrideUrl
        (this as? OkHttpTwsWebViewClient)?.dynamicModifiers = dynamicModifiers ?: emptyList()
    }
    chromeClient.state = state

    key(key) {
        AndroidView(
            factory = { context ->
                val factoryWebView: WebView
                val rootView: View

                if (isRefreshable) {
                    val layoutInflater = LayoutInflater.from(context)
                    rootView = layoutInflater.inflate(R.layout.swipe_refresh_webview, null)
                    val swipeRefreshLayout = rootView.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
                    factoryWebView = rootView.findViewById(R.id.web_view)

                    swipeRefreshLayout.setOnRefreshListener {
                        navigator.reload()
                    }
                } else {
                    rootView = WebView(context)
                    factoryWebView = rootView
                }

                createWebView(
                    context = context,
                    webView = factoryWebView,
                    state = state,
                    layoutParams = layoutParams,
                    factory = factory,
                    onCreated = { wv ->
                        onCreated(wv)
                        wv.setDownloadListener(TwsDownloadListener(context, wv) { permission, callback ->
                            permissionLauncher.launch(permission)
                            permissionCallback.value = callback
                        })
                    },
                    client = client,
                    chromeClient = chromeClient
                )

                rootView
            },
            modifier = modifier,
            onRelease = {
                val wv = it as? WebView ?: it.findViewById<WebView>(R.id.web_view)

                state.viewState = Bundle().apply {
                    wv.saveState(this)
                }.takeIf { !it.isEmpty } ?: state.viewState
                state.webView = null

                onDispose(wv)
            },
            update = {
                if (isRefreshable) {
                    val swipeRefreshLayout = it.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
                    swipeRefreshLayout.isRefreshing = state.isLoading
                }
            }
        )
    }
}

@Composable
private fun HandleBackPresses(captureBackPresses: Boolean, navigator: WebViewNavigator, webView: WebView?) {
    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }
}

@Composable
private fun HandleNavigationEvents(wv: WebView, navigator: WebViewNavigator, state: WebViewState) {
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

                is WebContent.Post -> {
                    wv.postUrl(
                        content.url,
                        content.postData
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
private fun WebViewResumeOrPauseEffect(webView: WebView?) {
    if (webView == null) return

    LifecycleResumeEffect(Unit) {
        webView.onResume()

        onPauseOrDispose {
            webView.onPause()
        }
    }
}

@Composable
private fun SetupPermissionHandling(
    chromeClient: TwsWebChromeClient,
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
private fun SetupFileChooserLauncher(chromeClient: TwsWebChromeClient) {
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
    webView: WebView,
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    factory: ((Context) -> WebView)?,
    onCreated: (WebView) -> Unit,
    client: WebViewClient,
    chromeClient: WebChromeClient
): WebView {
    return (factory?.invoke(context) ?: webView).apply {
        onCreated(this)

        this.layoutParams = layoutParams

        webChromeClient = chromeClient
        webViewClient = client

        state.viewState?.let { this.restoreState(it) }

        addJavascriptInterface(JavaScriptDownloadInterface(context), JAVASCRIPT_INTERFACE_NAME)
    }.also { state.webView = it }
}
