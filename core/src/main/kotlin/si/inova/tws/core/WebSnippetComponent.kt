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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import si.inova.tws.core.client.OkHttpTwsWebViewClient
import si.inova.tws.core.client.TwsWebChromeClient
import si.inova.tws.core.data.DeepLinkUrlLoadingCallback
import si.inova.tws.core.data.LoadingState
import si.inova.tws.core.data.UrlLoadingCallback
import si.inova.tws.core.data.WebContent
import si.inova.tws.core.data.WebViewNavigator
import si.inova.tws.core.data.WebViewState
import si.inova.tws.core.data.onCreateWindowStatus
import si.inova.tws.core.data.rememberSaveableWebViewState
import si.inova.tws.core.data.rememberWebViewNavigator
import si.inova.tws.core.util.compose.ErrorBannerWithSwipeToDismiss
import si.inova.tws.core.util.compose.SnippetErrorView
import si.inova.tws.core.util.compose.SnippetLoadingView
import si.inova.tws.core.util.compose.getUserFriendlyMessage
import si.inova.tws.core.util.initializeSettings
import si.inova.tws.data.DynamicResourceDto
import si.inova.tws.data.EngineType
import si.inova.tws.data.WebSnippetDto

/**
 *
 * WebSnippetComponent is a composable function that renders a WebView within a specified context,
 * allowing dynamic loading and interaction with web content. It provides various customizable options
 * to handle loading states, error handling, and URL interception.
 *
 * @param target An object that holds the necessary details to load and render a web snippet.
 * This includes the URL, custom HTTP headers, and any dynamic modifiers that might be applied to the web view.
 * @param modifier A compose modifier.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param webViewState State of WebView.
 * @param displayErrorViewOnError Whether to show a custom error view if loading the WebView content fails.
 * If set to true, the provided errorViewContent will be displayed in case of errors.
 * @param errorViewContent A custom composable that defines the UI content to display when there's an error
 * loading WebView content. Used only if [displayErrorViewOnError] is set to true.
 * @param displayPlaceholderWhileLoading If set to true, a placeholder or loading animation will be
 *  * shown while the WebView content is loading.
 * @param loadingPlaceholderContent A custom composable that defines the UI content to show while the WebView content is loading.
 *  Used only if [displayPlaceholderWhileLoading] is set to true.
 * @param urlLoadingCallback A lambda function that is invoked when a URL in WebView will be loaded.
 * Returning true prevents navigation to the new URL (and allowing you to define custom behavior for specific urls),
 * while returning false allows it to proceed.
 * @param googleLoginRedirectUrl A URL to which user is redirected after successful Google login. This will allow us to redirect
 * user back to the app after login in Custom Tabs has been completed.
 * @param isRefreshable if we allow to create pull to refresh
 */
@Composable
fun WebSnippetComponent(
    target: WebSnippetDto,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(target.id),
    webViewState: WebViewState = rememberSaveableWebViewState(target.id),
    displayErrorViewOnError: Boolean = false,
    errorViewContent: @Composable (String) -> Unit = { SnippetErrorView(it, false) },
    displayPlaceholderWhileLoading: Boolean = false,
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(false) },
    urlLoadingCallback: UrlLoadingCallback = DeepLinkUrlLoadingCallback(LocalContext.current),
    googleLoginRedirectUrl: String? = null,
    isRefreshable: Boolean = true
) {
    LaunchedEffect(navigator, target.loadIteration) {
        if (webViewState.viewState?.isEmpty != false || target.loadIteration != 0) {
            // This is the first time load, so load the home page, else it will be restored from bundle
            navigator.loadUrl(
                url = target.target,
                additionalHttpHeaders = target.headers.orEmpty()
            )
        }
    }

    val displayErrorContent = displayErrorViewOnError && webViewState.hasError
    val displayLoadingContent =
        displayPlaceholderWhileLoading && webViewState.loadingState is LoadingState.Loading

    val popupStates = remember { mutableStateOf<List<WebViewState>>(emptyList()) }
    val popupStateCallback: (WebViewState, Boolean) -> Unit = { state, isAdd ->
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
        key = target.id,
        navigator = navigator,
        webViewState = webViewState,
        displayLoadingContent = displayLoadingContent,
        loadingPlaceholderContent = loadingPlaceholderContent,
        displayErrorContent = displayErrorContent,
        urlLoadingCallback = urlLoadingCallback,
        errorViewContent = errorViewContent,
        popupStateCallback = popupStateCallback,
        dynamicModifiers = target.dynamicResources.toImmutableList(),
        mustacheProps = target.props.toImmutableMap(),
        targetEngine = target.engine,
        isRefreshable = isRefreshable
    )

    popupStates.value.forEach { state ->
        val msgState = state.content as WebContent.MessageOnly
        PopUpWebView(
            popupState = state,
            displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
            loadingPlaceholderContent = loadingPlaceholderContent,
            displayErrorViewOnError = displayErrorViewOnError,
            errorViewContent = errorViewContent,
            onDismissRequest = { popupStates.value = popupStates.value.filter { it != state } },
            popupStateCallback = popupStateCallback,
            urlLoadingCallback = urlLoadingCallback,
            googleLoginRedirectUrl = googleLoginRedirectUrl,
            dynamicModifiers = target.dynamicResources.toImmutableList(),
            isFullscreen = !msgState.isDialog
        )
    }
}

@Composable
private fun SnippetContentWithLoadingAndError(
    key: String,
    navigator: WebViewNavigator,
    webViewState: WebViewState,
    displayLoadingContent: Boolean,
    loadingPlaceholderContent: @Composable () -> Unit,
    displayErrorContent: Boolean,
    isRefreshable: Boolean,
    errorViewContent: @Composable (String) -> Unit,
    urlLoadingCallback: UrlLoadingCallback,
    modifier: Modifier = Modifier,
    onCreated: (WebView) -> Unit = {},
    popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null,
    dynamicModifiers: ImmutableList<DynamicResourceDto> = persistentListOf(),
    mustacheProps: ImmutableMap<String, Any> = persistentMapOf(),
    targetEngine: EngineType? = null
) {
    // https://github.com/google/accompanist/issues/1326 - WebView settings does not work in compose preview
    val isPreviewMode = LocalInspectionMode.current
    val client = remember(key1 = key) {
        OkHttpTwsWebViewClient(urlLoadingCallback, popupStateCallback).apply {
            setDynamicModifiers(dynamicModifiers)
            setMustacheProps(mustacheProps, targetEngine)
        }
    }
    val chromeClient = remember(key1 = key) { TwsWebChromeClient(popupStateCallback) }

    LaunchedEffect(dynamicModifiers) {
        if (!client.setDynamicModifiers(dynamicModifiers)) {
            navigator.reload()
        }
    }

    LaunchedEffect(mustacheProps, targetEngine) {
        if (!client.setMustacheProps(mustacheProps, targetEngine)) {
            navigator.reload()
        }
    }

    Box(modifier = modifier) {
        WebView(
            key = key,
            modifier = Modifier.fillMaxSize(),
            state = webViewState,
            navigator = navigator,
            onCreated = {
                if (!isPreviewMode) it.initializeSettings()
                onCreated(it)
            },
            client = client,
            chromeClient = chromeClient,
            isRefreshable = isRefreshable
        )

        if (displayLoadingContent) {
            loadingPlaceholderContent()
        }

        SnippetErrors(displayErrorContent, webViewState, errorViewContent)
    }
}

@Composable
private fun SnippetErrors(
    displayErrorContent: Boolean,
    webViewState: WebViewState,
    errorViewContent: @Composable (String) -> Unit,
) {
    if (displayErrorContent) {
        val message = webViewState.webViewErrorsForCurrentRequest.firstOrNull()?.error?.description?.toString()
            ?: stringResource(id = R.string.oops_loading_failed)

        errorViewContent(message)
    }

    if (webViewState.customErrorsForCurrentRequest.size > 0 && !displayErrorContent) {
        val error = webViewState.customErrorsForCurrentRequest.first()
        if (error is MustacheException) {
            errorViewContent(error.message ?: error.getUserFriendlyMessage())
        } else {
            ErrorBannerWithSwipeToDismiss(error.getUserFriendlyMessage())
        }
    }
}

@Composable
private fun PopUpWebView(
    popupState: WebViewState,
    displayPlaceholderWhileLoading: Boolean,
    loadingPlaceholderContent: @Composable () -> Unit,
    displayErrorViewOnError: Boolean,
    errorViewContent: @Composable (String) -> Unit,
    onDismissRequest: () -> Unit,
    urlLoadingCallback: UrlLoadingCallback,
    popupNavigator: WebViewNavigator = rememberWebViewNavigator(),
    popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null,
    googleLoginRedirectUrl: String? = null,
    dynamicModifiers: ImmutableList<DynamicResourceDto> = persistentListOf(),
    mustacheProps: ImmutableMap<String, Any> = persistentMapOf(),
    isRefreshable: Boolean = false,
    isFullscreen: Boolean = false
) {
    val displayErrorContent = displayErrorViewOnError && popupState.hasError
    val displayLoadingContent = displayPlaceholderWhileLoading && popupState.loadingState is LoadingState.Loading

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
                webViewState = popupState,
                displayLoadingContent = displayLoadingContent,
                loadingPlaceholderContent = loadingPlaceholderContent,
                displayErrorContent = displayErrorContent,
                errorViewContent = errorViewContent,
                onCreated = (popupState.content as WebContent.MessageOnly)::onCreateWindowStatus,
                popupStateCallback = popupStateCallback,
                urlLoadingCallback = urlLoadingCallback,
                dynamicModifiers = dynamicModifiers,
                mustacheProps = mustacheProps,
                isRefreshable = isRefreshable
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
private fun WebSnippetComponentPreview() {
    WebSnippetComponent(
        WebSnippetDto(
            id = "id",
            target = "https://www.google.com/",
            projectId = "projId",
            organizationId = "orgId"
        )
    )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderComponentPreview() {
    WebSnippetComponent(
        WebSnippetDto(
            id = "id", target = "https://www.google.com/",
            projectId = "projId",
            organizationId = "orgId"
        ),
        webViewState = webStateLoading,
        displayErrorViewOnError = true,
        displayPlaceholderWhileLoading = true
    )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderInitComponentPreview() {
    WebSnippetComponent(
        WebSnippetDto(
            id = "id", target = "https://www.google.com/",
            projectId = "projId",
            organizationId = "orgId"
        ),
        webViewState = webStateInitializing,
        displayErrorViewOnError = true,
        displayPlaceholderWhileLoading = true
    )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderFinishedComponentPreview() {
    WebSnippetComponent(
        WebSnippetDto(
            id = "id", target = "https://www.google.com/",
            projectId = "projId",
            organizationId = "orgId"
        ),
        webViewState = webStateLoadingFinished,
        displayErrorViewOnError = true,
        displayPlaceholderWhileLoading = true
    )
}

private val webStateInitializing = WebViewState(WebContent.NavigatorOnly).apply { loadingState = LoadingState.Initializing }
private val webStateLoading = WebViewState(WebContent.NavigatorOnly).apply { loadingState = LoadingState.Loading(0.5f) }
private val webStateLoadingFinished = WebViewState(WebContent.NavigatorOnly).apply { loadingState = LoadingState.Finished }
