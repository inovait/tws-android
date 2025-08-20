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
import com.thewebsnippet.view.data.getSnippet
import com.thewebsnippet.view.data.onCreateWindowStatus
import com.thewebsnippet.view.data.rememberTWSViewNavigator
import com.thewebsnippet.view.data.rememberTWSViewState
import com.thewebsnippet.view.util.compose.error.SnippetErrorView
import com.thewebsnippet.view.util.compose.SnippetLoadingView
import com.thewebsnippet.view.util.compose.error.ErrorRefreshCallback
import com.thewebsnippet.view.util.compose.error.SnippetErrorContent
import com.thewebsnippet.view.util.compose.error.defaultErrorView
import com.thewebsnippet.view.util.compose.getUserFriendlyMessage
import com.thewebsnippet.view.util.compose.isRefreshable
import com.thewebsnippet.view.util.initializeSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

/**
 *
 * TWSView is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param viewState The current [TWSViewState] representing the state of the WebView.
 * @param modifier A [Modifier] to additionally customize the layout of the WebView.
 * @param navigator The current [TWSViewNavigator] to control WebView navigation externally.
 * @param errorViewContent A custom composable displayed when there is an error loading content.
 * Defaults to a [SnippetErrorView] with the same modifier as [TWSView].
 * @param loadingPlaceholderContent A custom composable displayed during loading.
 * Defaults to a [SnippetLoadingView] with the same modifier as [TWSView] that is displayed until mainFrame content is loaded.
 * @param interceptUrlCallback A [TWSViewInterceptor] invoked for URLs before navigation.
 * Return `true` to prevent navigation, `false` to allow it.
 * @param googleLoginRedirectUrl The URL the app should redirect to after a Google login
 * via [Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs).
 * Allows returning users to the app after authentication.
 * @param isRefreshable Enables pull-to-refresh action when set to `true`.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 *
 * Users can customize the colors used in the pull-to-refresh SwipeRefreshLayout by overriding the
 * following theme attributes in their app theme:
 *
 * - `twsViewSwipeRefreshSpinnerColor`: The color of the spinner (progress indicator)
 * - `twsViewSwipeRefreshBackgroundColor`: The background color behind the spinner
 *
 * Example usage in the app theme:
 *
 * ```
 * <style name="AppTheme" parent="Theme.MaterialComponents.DayNight">
 *     <item name="twsViewSwipeRefreshSpinnerColor">#FF4081</item>
 *     <item name="twsViewSwipeRefreshBackgroundColor">#EEEEEE</item>
 * </style>
 * ```
 */
@Composable
fun TWSView(
    viewState: TWSViewState,
    modifier: Modifier = Modifier,
    navigator: TWSViewNavigator = rememberTWSViewNavigator(),
    errorViewContent: SnippetErrorContent = defaultErrorView(modifier),
    loadingPlaceholderContent: @Composable (TWSLoadingState.Loading) -> Unit = { SnippetLoadingView(it, modifier) },
    interceptUrlCallback: TWSViewInterceptor = TWSViewDeepLinkInterceptor(LocalContext.current),
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true,
    captureBackPresses: Boolean = true,
    onCreated: (WebView) -> Unit = {}
) {
    key(viewState.content) {
        LaunchedEffect(navigator) {
            val content = viewState.content as? WebContent.NavigatorOnly
            if (viewState.viewStatePath == null && content?.default != null) {
                // Handle first time load for navigator only state, other loads will be handled with state restoration
                navigator.loadSnippet(content.default)
            }
        }

        SnippetContentWithPopup(
            navigator = navigator,
            viewState = viewState,
            errorViewContent = errorViewContent,
            loadingPlaceholderContent = loadingPlaceholderContent,
            interceptUrlCallback = interceptUrlCallback,
            googleLoginRedirectUrl = googleLoginRedirectUrl,
            isRefreshable = isRefreshable,
            captureBackPresses = captureBackPresses,
            modifier = modifier,
            onCreated = onCreated,
        )
    }
}

/**
 *
 * TWSView is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param snippet A [TWSSnippet] containing the URL, custom HTTP headers, and modifiers
 * for the web snippet to be rendered.
 * @param modifier A [Modifier] to additionally customize the layout of the WebView.
 * @param errorViewContent A custom composable displayed when there is an error loading content.
 * Defaults to a [SnippetLoadingView] with the same modifier as [TWSView] that is displayed until mainFrame content is loaded.
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
 *
 * Users can customize the colors used in the pull-to-refresh SwipeRefreshLayout by overriding the
 * following theme attributes in their app theme:
 *
 * - `twsViewSwipeRefreshSpinnerColor`: The color of the spinner (progress indicator)
 * - `twsViewSwipeRefreshBackgroundColor`: The background color behind the spinner
 *
 * Example usage in the app theme:
 *
 * ```
 * <style name="AppTheme" parent="Theme.MaterialComponents.DayNight">
 *     <item name="twsViewSwipeRefreshSpinnerColor">#FF4081</item>
 *     <item name="twsViewSwipeRefreshBackgroundColor">#EEEEEE</item>
 * </style>
 * ```
 */
@Composable
fun TWSView(
    snippet: TWSSnippet,
    modifier: Modifier = Modifier,
    errorViewContent: SnippetErrorContent = defaultErrorView(modifier),
    loadingPlaceholderContent: @Composable (TWSLoadingState.Loading) -> Unit = { SnippetLoadingView(it, modifier) },
    interceptUrlCallback: TWSViewInterceptor = TWSViewDeepLinkInterceptor(LocalContext.current),
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true,
    captureBackPresses: Boolean = true,
    onCreated: (WebView) -> Unit = {}
) {
    val navigator = rememberTWSViewNavigator(snippet)
    val viewState = rememberTWSViewState(snippet)

    TWSView(
        viewState = viewState,
        modifier = modifier,
        navigator = navigator,
        errorViewContent = errorViewContent,
        loadingPlaceholderContent = loadingPlaceholderContent,
        interceptUrlCallback = interceptUrlCallback,
        googleLoginRedirectUrl = googleLoginRedirectUrl,
        isRefreshable = isRefreshable,
        captureBackPresses = captureBackPresses,
        onCreated = onCreated
    )
}

/**
 * Renders a WebView with popup support for handling separate web views in dialog-like containers.
 * Popups are displayed in dialogs, and navigation can be controlled externally.
 *
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
    navigator: TWSViewNavigator,
    viewState: TWSViewState,
    errorViewContent: SnippetErrorContent,
    loadingPlaceholderContent: @Composable (TWSLoadingState.Loading) -> Unit,
    interceptUrlCallback: TWSViewInterceptor,
    googleLoginRedirectUrl: String?,
    isRefreshable: Boolean,
    captureBackPresses: Boolean,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {}
) {
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

    val target = viewState.content.getSnippet()

    SnippetContentWithLoadingAndError(
        modifier = modifier,
        key = target?.let { "${target.id}-${target.target}" },
        navigator = navigator,
        viewState = viewState,
        loadingPlaceholderContent = loadingPlaceholderContent,
        errorViewContent = errorViewContent,
        interceptUrlCallback = interceptUrlCallback,
        popupStateCallback = popupStateCallback,
        isRefreshable = isRefreshable,
        onCreated = onCreated,
        captureBackPresses = captureBackPresses,
        dynamicModifiers = target?.dynamicResources?.toPersistentList() ?: persistentListOf(),
        mustacheProps = target?.props?.toPersistentMap() ?: persistentMapOf(),
        engine = target?.engine ?: TWSEngine.NONE
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
            isFullscreen = !msgState.isDialog,
            isRefreshable = isRefreshable,
            captureBackPresses = true
        )
    }
}

@Composable
private fun SnippetContentWithLoadingAndError(
    navigator: TWSViewNavigator,
    viewState: TWSViewState,
    loadingPlaceholderContent: @Composable (TWSLoadingState.Loading) -> Unit,
    errorViewContent: SnippetErrorContent,
    interceptUrlCallback: TWSViewInterceptor,
    popupStateCallback: ((TWSViewState, Boolean) -> Unit)?,
    isRefreshable: Boolean,
    captureBackPresses: Boolean,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {},
    key: String? = null,
    dynamicModifiers: ImmutableList<TWSAttachment> = persistentListOf(),
    mustacheProps: ImmutableMap<String, Any> = persistentMapOf(),
    engine: TWSEngine = TWSEngine.NONE,
) {
    // https://github.com/google/accompanist/issues/1326 - WebView settings does not work in compose preview
    val isPreviewMode = LocalInspectionMode.current
    val client = remember(key1 = key) {
        OkHttpTWSWebViewClient(
            dynamicModifiers = dynamicModifiers,
            mustacheProps = mustacheProps,
            engine = engine,
            interceptUrlCallback = interceptUrlCallback,
            popupStateCallback = popupStateCallback
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

        // display loading view, user can still override placeholder and return if he wants to hide loading view
        // when certain conditions are met (i.e. see default implementation)
        (viewState.loadingState as? TWSLoadingState.Loading)?.let {
            loadingPlaceholderContent(it)
        }

        SnippetErrors(viewState, errorViewContent) {
            val isInitialRequestError = viewState.customErrorsForCurrentRequest.isNotEmpty()
            if (isInitialRequestError) {
                // if initial request to snippet target fails, we have to reload snippet, since web view has no data
                val snippet = viewState.content.getSnippet() ?: return@SnippetErrors
                navigator.loadSnippet(snippet)
            } else {
                // if any web view navigation loading event fails, we can reload
                navigator.reload()
            }
        }
    }
}

@Composable
private fun SnippetErrors(
    viewState: TWSViewState,
    errorViewContent: SnippetErrorContent,
    refreshCallback: ErrorRefreshCallback
) {
    if (viewState.webViewErrorsForCurrentRequest.any { it.request?.isForMainFrame == true }) {
        val error = viewState.webViewErrorsForCurrentRequest.firstOrNull()?.error
        val message = error?.getUserFriendlyMessage() ?: error?.description?.toString()
            ?: stringResource(id = R.string.oops_loading_failed)

        errorViewContent(message, null, false)
    }

    if (viewState.customErrorsForCurrentRequest.size > 0) {
        val error = viewState.customErrorsForCurrentRequest.first()
        val message = error.getUserFriendlyMessage() ?: error.message ?: stringResource(R.string.error_general)

        errorViewContent(message, refreshCallback, error.isRefreshable())
    }
}

@Composable
private fun PopUpWebView(
    popupState: TWSViewState,
    loadingPlaceholderContent: @Composable (TWSLoadingState.Loading) -> Unit,
    errorViewContent: SnippetErrorContent,
    onDismissRequest: () -> Unit,
    interceptUrlCallback: TWSViewInterceptor,
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
    TWSView(viewState = webStateLoading)
}

@Composable
@Preview
private fun WebSnippetLoadingForceRefreshComponentPreview() {
    TWSView(viewState = webStateLoadingForceRefresh)
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderInitComponentPreview() {
    TWSView(viewState = webStateInitializing)
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderFinishedComponentPreview() {
    TWSView(viewState = webStateLoadingFinished)
}

private val webStateInitializing = TWSViewState(WebContent.NavigatorOnly(TWSSnippet("id", "url"))).apply {
    loadingState = TWSLoadingState.Initializing
}
private val webStateLoading = TWSViewState(WebContent.NavigatorOnly(TWSSnippet("id", "url"))).apply {
    loadingState = TWSLoadingState.Loading(0.5f, false)
}
private val webStateLoadingForceRefresh = TWSViewState(WebContent.NavigatorOnly(TWSSnippet("id", "url"))).apply {
    loadingState = TWSLoadingState.Loading(0.5f, true)
}
private val webStateLoadingFinished = TWSViewState(WebContent.NavigatorOnly(TWSSnippet("id", "url"))).apply {
    loadingState = TWSLoadingState.Finished
}
