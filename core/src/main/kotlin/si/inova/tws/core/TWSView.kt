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

package si.inova.tws.core

import android.content.Intent
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.util.Consumer
import com.samskivert.mustache.MustacheException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import si.inova.tws.core.client.OkHttpTWSWebViewClient
import si.inova.tws.core.client.TWSWebChromeClient
import si.inova.tws.core.data.TWSDeepLinkInterceptUrlCallback
import si.inova.tws.core.data.LoadingState
import si.inova.tws.core.data.TWSInterceptUrlCallback
import si.inova.tws.core.data.WebContent
import si.inova.tws.core.data.TWSViewNavigator
import si.inova.tws.core.data.TWSViewState
import si.inova.tws.core.data.onCreateWindowStatus
import si.inova.tws.core.data.rememberTWSViewNavigator
import si.inova.tws.core.data.rememberTWSViewState
import si.inova.tws.core.util.compose.ErrorBannerWithSwipeToDismiss
import si.inova.tws.core.util.compose.SnippetErrorView
import si.inova.tws.core.util.compose.SnippetLoadingView
import si.inova.tws.core.util.compose.getUserFriendlyMessage
import si.inova.tws.core.util.initializeSettings
import si.inova.tws.data.TWSAttachment
import si.inova.tws.data.TWSEngine
import si.inova.tws.data.TWSSnippet

/**
 *
 * WebSnippetComponent is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param snippet An object that holds the necessary details to load and render a web snippet.
 * This includes the URL, custom HTTP headers, and any dynamic modifiers that might be applied to the web view.
 * @param modifier A compose modifier.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param viewState State of WebView.
 * @param errorViewContent A custom composable that defines the UI content to display when there's an error
 * loading WebView content.
 * @param loadingPlaceholderContent A custom composable that defines the UI content to show while the WebView content is loading.
 * @param interceptUrlCallback A lambda function that is invoked when a URL in WebView will be loaded.
 * Returning true prevents navigation to the new URL (and allowing you to define custom behavior for specific urls),
 * while returning false allows it to proceed.
 * @param googleLoginRedirectUrl A URL to which user is redirected after successful Google login. This will allow us to redirect
 * user back to the app after login in Custom Tabs has been completed.
 * @param isRefreshable if we allow to create pull to refresh
 */
@Composable
fun TWSView(
    snippet: TWSSnippet,
    modifier: Modifier = Modifier,
    navigator: TWSViewNavigator = rememberTWSViewNavigator("${snippet.id}:${snippet.target}"),
    viewState: TWSViewState = rememberTWSViewState(snippet, "${snippet.id}:${snippet.target}"),
    errorViewContent: @Composable (String) -> Unit = { SnippetErrorView(it, true) },
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(true) },
    interceptUrlCallback: TWSInterceptUrlCallback = TWSDeepLinkInterceptUrlCallback(LocalContext.current),
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true
) {
    key(snippet) {
        SnippetContentWithPopup(
            snippet,
            navigator,
            viewState,
            errorViewContent,
            loadingPlaceholderContent,
            interceptUrlCallback,
            googleLoginRedirectUrl,
            isRefreshable,
            modifier
        )
    }
}

@Composable
private fun SnippetContentWithPopup(
    target: TWSSnippet,
    navigator: TWSViewNavigator,
    viewState: TWSViewState,
    errorViewContent: @Composable (String) -> Unit,
    loadingPlaceholderContent: @Composable () -> Unit,
    interceptUrlCallback: TWSInterceptUrlCallback,
    googleLoginRedirectUrl: String?,
    isRefreshable: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(navigator) {
        if (viewState.viewState?.isEmpty != false && viewState.content is WebContent.NavigatorOnly) {
            // Handle first time load for navigator only state, other loads will be handled with state restoration
            navigator.loadUrl(
                url = target.target,
                additionalHttpHeaders = target.headers.orEmpty()
            )
        }
    }

    val popupStates = remember { mutableStateOf<List<TWSViewState>>(emptyList()) }
    val popupStateCallback: (TWSViewState, Boolean) -> Unit = { state, isAdd ->
        popupStates.value = if (isAdd) {
            val oldList = popupStates.value.toMutableList().apply {
                add(state)
            }

            oldList.toList()
        } else {
            popupStates.value.filter { it != state }
        }
    }

    googleLoginRedirectUrl?.let {
        NewIntentListener { intent ->
            val data = intent.data?.toString()
            if (popupStates.value.isEmpty() && data?.startsWith(it) == true) {
                navigator.loadUrl(data)
            }
        }
    }

    SnippetContentWithLoadingAndError(
        modifier = modifier,
        key = "${target.id}-${target.target}",
        navigator = navigator,
        viewState = viewState,
        loadingPlaceholderContent = loadingPlaceholderContent,
        errorViewContent = errorViewContent,
        interceptUrlCallback = interceptUrlCallback,
        popupStateCallback = popupStateCallback,
        dynamicModifiers = target.dynamicResources.toImmutableList(),
        mustacheProps = target.props.toImmutableMap(),
        engine = target.engine,
        isRefreshable = isRefreshable
    )

    popupStates.value.forEach { state ->
        val msgState = state.content as WebContent.MessageOnly
        PopUpWebView(
            popupState = state,
            loadingPlaceholderContent = loadingPlaceholderContent,
            errorViewContent = errorViewContent,
            onDismissRequest = { popupStates.value = popupStates.value.filter { it != state } },
            popupStateCallback = popupStateCallback,
            interceptUrlCallback = interceptUrlCallback,
            googleLoginRedirectUrl = googleLoginRedirectUrl,
            dynamicModifiers = target.dynamicResources.toImmutableList(),
            mustacheProps = target.props.toImmutableMap(),
            engine = target.engine,
            isFullscreen = !msgState.isDialog,
            isRefreshable = isRefreshable
        )
    }
}

@Composable
private fun SnippetContentWithLoadingAndError(
    key: String,
    navigator: TWSViewNavigator,
    viewState: TWSViewState,
    loadingPlaceholderContent: @Composable () -> Unit,
    errorViewContent: @Composable (String) -> Unit,
    interceptUrlCallback: TWSInterceptUrlCallback,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)?,
    dynamicModifiers: ImmutableList<TWSAttachment>,
    mustacheProps: ImmutableMap<String, Any>,
    engine: TWSEngine,
    isRefreshable: Boolean,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {}
) {
    // https://github.com/google/accompanist/issues/1326 - WebView settings does not work in compose preview
    val isPreviewMode = LocalInspectionMode.current
    val client = remember(key1 = key) {
        OkHttpTWSWebViewClient(
            dynamicModifiers,
            mustacheProps,
            engine,
            interceptUrlCallback,
            popupStateCallback
        )
    }
    val chromeClient = remember(key1 = key) { TWSWebChromeClient(popupStateCallback) }

    Box(modifier = modifier) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            state = viewState,
            navigator = navigator,
            onCreated = {
                if (!isPreviewMode) it.initializeSettings()
                onCreated(it)
            },
            client = client,
            chromeClient = chromeClient,
            isRefreshable = isRefreshable
        )

        SnippetLoading(viewState, loadingPlaceholderContent)

        SnippetErrors(viewState, errorViewContent)
    }
}

@Composable
private fun SnippetErrors(
    viewState: TWSViewState,
    errorViewContent: @Composable (String) -> Unit,
) {
    if (viewState.webViewErrorsForCurrentRequest.any { it.request?.isForMainFrame == true }) {
        val message = viewState.webViewErrorsForCurrentRequest.firstOrNull()?.error?.description?.toString()
            ?: stringResource(id = R.string.oops_loading_failed)

        errorViewContent(message)
    }

    if (viewState.customErrorsForCurrentRequest.size > 0) {
        val error = viewState.customErrorsForCurrentRequest.first()
        if (error is MustacheException) {
            errorViewContent(error.message ?: error.getUserFriendlyMessage())
        } else {
            ErrorBannerWithSwipeToDismiss(error.getUserFriendlyMessage())
        }
    }
}

@Composable
private fun SnippetLoading(
    viewState: TWSViewState,
    loadingPlaceholderContent: @Composable () -> Unit
) {
    val state = viewState.loadingState
    if (state is LoadingState.Loading && !state.isUserForceRefresh) {
        loadingPlaceholderContent()
    }
}

@Composable
private fun PopUpWebView(
    popupState: TWSViewState,
    loadingPlaceholderContent: @Composable () -> Unit,
    errorViewContent: @Composable (String) -> Unit,
    onDismissRequest: () -> Unit,
    interceptUrlCallback: TWSInterceptUrlCallback,
    dynamicModifiers: ImmutableList<TWSAttachment>,
    mustacheProps: ImmutableMap<String, Any>,
    engine: TWSEngine,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)?,
    googleLoginRedirectUrl: String?,
    isRefreshable: Boolean,
    isFullscreen: Boolean,
    popupNavigator: TWSViewNavigator = rememberTWSViewNavigator()
) {
    googleLoginRedirectUrl?.let {
        NewIntentListener { intent ->
            val data = intent.data?.toString()
            if (data?.startsWith(it) == true) {
                popupNavigator.loadUrl(data)
            }
        }
    }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = !isFullscreen),
        onDismissRequest = {
            popupState.webView?.destroy()
            onDismissRequest()
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight(if (isFullscreen) 1f else WEB_VIEW_POPUP_HEIGHT_PERCENTAGE)
                .fillMaxWidth(if (isFullscreen) 1f else WEB_VIEW_POPUP_WIDTH_PERCENTAGE),
            shape = if (isFullscreen) RectangleShape else RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            SnippetContentWithLoadingAndError(
                key = "popup",
                navigator = popupNavigator,
                viewState = popupState,
                loadingPlaceholderContent = loadingPlaceholderContent,
                errorViewContent = errorViewContent,
                popupStateCallback = popupStateCallback,
                interceptUrlCallback = interceptUrlCallback,
                dynamicModifiers = dynamicModifiers,
                mustacheProps = mustacheProps,
                engine = engine,
                isRefreshable = isRefreshable,
                onCreated = (popupState.content as WebContent.MessageOnly)::onCreateWindowStatus
            )
        }
    }
}

@Composable
private fun NewIntentListener(callback: (Intent) -> Unit) {
    val activity = LocalContext.current as? ComponentActivity
    DisposableEffect(Unit) {
        val newIntentListener = Consumer<Intent> { intent ->
            callback(intent)
        }

        activity?.addOnNewIntentListener(newIntentListener)

        onDispose {
            activity?.removeOnNewIntentListener(newIntentListener)
        }
    }
}

private const val WEB_VIEW_POPUP_WIDTH_PERCENTAGE = 0.95f
private const val WEB_VIEW_POPUP_HEIGHT_PERCENTAGE = 0.8f

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderComponentPreview() {
    TWSView(
        TWSSnippet(id = "id", target = "https://www.google.com/"),
        viewState = webStateLoading
    )
}

@Composable
@Preview
private fun WebSnippetLoadingForceRefreshComponentPreview() {
    TWSView(
        TWSSnippet(id = "id", target = "https://www.google.com/"),
        viewState = webStateLoadingForceRefresh
    )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderInitComponentPreview() {
    TWSView(
        TWSSnippet(id = "id", target = "https://www.google.com/"),
        viewState = webStateInitializing
    )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderFinishedComponentPreview() {
    TWSView(
        TWSSnippet(id = "id", target = "https://www.google.com/"),
        viewState = webStateLoadingFinished
    )
}

private val webStateInitializing = TWSViewState(WebContent.NavigatorOnly).apply { loadingState = LoadingState.Initializing }
private val webStateLoading = TWSViewState(WebContent.NavigatorOnly).apply {
    loadingState = LoadingState.Loading(0.5f, false)
}
private val webStateLoadingForceRefresh = TWSViewState(WebContent.NavigatorOnly).apply {
    loadingState = LoadingState.Loading(0.5f, true)
}
private val webStateLoadingFinished = TWSViewState(WebContent.NavigatorOnly).apply { loadingState = LoadingState.Finished }
