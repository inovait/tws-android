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
package com.thewebsnippet.view

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
import com.thewebsnippet.data.TWSAttachment
import com.thewebsnippet.data.TWSEngine
import com.thewebsnippet.data.TWSSnippet
import com.thewebsnippet.view.client.OkHttpTWSWebViewClient
import com.thewebsnippet.view.client.TWSWebChromeClient
import com.thewebsnippet.view.data.TWSLoadingState
import com.thewebsnippet.view.data.TWSViewDeepLinkInterceptor
import com.thewebsnippet.view.data.TWSViewInterceptor
import com.thewebsnippet.view.data.TWSViewNavigator
import com.thewebsnippet.view.data.TWSViewState
import com.thewebsnippet.view.data.WebContent
import com.thewebsnippet.view.data.onCreateWindowStatus
import com.thewebsnippet.view.data.rememberTWSViewNavigator
import com.thewebsnippet.view.data.rememberTWSViewState
import com.thewebsnippet.view.util.compose.ErrorBannerWithSwipeToDismiss
import com.thewebsnippet.view.util.compose.SnippetErrorView
import com.thewebsnippet.view.util.compose.SnippetLoadingView
import com.thewebsnippet.view.util.compose.getUserFriendlyMessage
import com.thewebsnippet.view.util.initializeSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 *
 * TWSView is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param snippet A [TWSSnippet] containing the URL, custom HTTP headers, and modifiers
 * for the web snippet to be rendered.
 * @param modifier A [Modifier] to additionally customize the layout of the WebView.
 * @param navigator The current [TWSViewNavigator] to control WebView navigation externally.
 * @param viewState The current [TWSViewState] representing the state of the WebView.
 * @param errorViewContent A custom composable displayed when there is an error loading content.
 * Defaults to a [SnippetErrorView] with the same modifier as [TWSView].
 * @param loadingPlaceholderContent A custom composable displayed during loading.
 * Defaults to a [SnippetLoadingView] with the same modifier as [TWSView].
 * @param interceptUrlCallback A [TWSViewInterceptor] invoked for URLs before navigation.
 * Return `true` to prevent navigation, `false` to allow it.
 * @param googleLoginRedirectUrl The URL the app should redirect to after a Google login
 * via [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs).
 * Allows returning users to the app after authentication.
 * @param isRefreshable Enables pull-to-refresh functionality when set to `true`.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 */
@Composable
fun TWSView(
    snippet: TWSSnippet,
    modifier: Modifier = Modifier,
    navigator: TWSViewNavigator = rememberTWSViewNavigator(snippet),
    viewState: TWSViewState = rememberTWSViewState(snippet),
    errorViewContent: @Composable (String) -> Unit = { SnippetErrorView(it, modifier) },
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(modifier) },
    interceptUrlCallback: TWSViewInterceptor = TWSViewDeepLinkInterceptor(LocalContext.current),
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true,
    captureBackPresses: Boolean = true,
    onCreated: (WebView) -> Unit = {}
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
            captureBackPresses,
            modifier,
            onCreated
        )
    }
}

/**
 * Renders a WebView with popup support for handling separate web views in dialog-like containers.
 * Popups are displayed in dialogs, and navigation can be controlled externally.
 *
 * @param target The [TWSSnippet] to load in the WebView.
 * @param navigator The [TWSViewNavigator] for controlling navigation in the WebView.
 * @param viewState The [TWSViewState] representing the state of the WebView.
 * @param errorViewContent The composable content for rendering error messages.
 * @param loadingPlaceholderContent The composable content for rendering a loading state.
 * @param interceptUrlCallback A [TWSViewInterceptor] for handling intercepted URLs.
 * @param googleLoginRedirectUrl URL to redirect back after Google login via
 * [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs).
 * @param isRefreshable Enables pull-to-refresh functionality.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param modifier A [Modifier] to configure the layout or styling of the error view.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 */
@Composable
private fun SnippetContentWithPopup(
    target: TWSSnippet,
    navigator: TWSViewNavigator,
    viewState: TWSViewState,
    errorViewContent: @Composable (String) -> Unit,
    loadingPlaceholderContent: @Composable () -> Unit,
    interceptUrlCallback: TWSViewInterceptor,
    googleLoginRedirectUrl: String?,
    isRefreshable: Boolean,
    captureBackPresses: Boolean,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {}
) {
    LaunchedEffect(navigator) {
        if (viewState.viewState?.isEmpty != false && viewState.content is WebContent.NavigatorOnly) {
            // Handle first time load for navigator only state, other loads will be handled with state restoration
            navigator.loadUrl(
                url = target.target,
                additionalHttpHeaders = target.headers
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
        isRefreshable = isRefreshable,
        onCreated = onCreated,
        captureBackPresses = captureBackPresses
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
            isRefreshable = isRefreshable,
            captureBackPresses = true
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
    interceptUrlCallback: TWSViewInterceptor,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)?,
    dynamicModifiers: ImmutableList<TWSAttachment>,
    mustacheProps: ImmutableMap<String, Any>,
    engine: TWSEngine,
    isRefreshable: Boolean,
    captureBackPresses: Boolean,
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
            isRefreshable = isRefreshable,
            captureBackPresses = captureBackPresses
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
        val message = error.getUserFriendlyMessage() ?: error.message ?: stringResource(R.string.error_general)
        if (error is MustacheException) {
            errorViewContent(message)
        } else {
            ErrorBannerWithSwipeToDismiss(message)
        }
    }
}

@Composable
private fun SnippetLoading(
    viewState: TWSViewState,
    loadingPlaceholderContent: @Composable () -> Unit
) {
    val state = viewState.mainLoadingState
    if (state is TWSLoadingState.Loading && !state.isUserForceRefresh) {
        loadingPlaceholderContent()
    }
}

@Composable
private fun PopUpWebView(
    popupState: TWSViewState,
    loadingPlaceholderContent: @Composable () -> Unit,
    errorViewContent: @Composable (String) -> Unit,
    onDismissRequest: () -> Unit,
    interceptUrlCallback: TWSViewInterceptor,
    dynamicModifiers: ImmutableList<TWSAttachment>,
    mustacheProps: ImmutableMap<String, Any>,
    engine: TWSEngine,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)?,
    googleLoginRedirectUrl: String?,
    isRefreshable: Boolean,
    captureBackPresses: Boolean,
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
                captureBackPresses = captureBackPresses,
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

private val webStateInitializing = TWSViewState(WebContent.NavigatorOnly).apply {
    mainLoadingState = TWSLoadingState.Initializing
}
private val webStateLoading = TWSViewState(WebContent.NavigatorOnly).apply {
    mainLoadingState = TWSLoadingState.Loading(0.5f, false)
}
private val webStateLoadingForceRefresh = TWSViewState(WebContent.NavigatorOnly).apply {
    mainLoadingState = TWSLoadingState.Loading(0.5f, true)
}
private val webStateLoadingFinished = TWSViewState(WebContent.NavigatorOnly).apply { mainLoadingState = TWSLoadingState.Finished }
