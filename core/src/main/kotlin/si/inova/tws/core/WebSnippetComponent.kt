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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.util.Consumer
import si.inova.tws.core.data.view.LoadingState
import si.inova.tws.core.data.ModifierPageData
import si.inova.tws.core.data.view.WebContent
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.core.data.view.WebViewNavigator
import si.inova.tws.core.data.view.WebViewState
import si.inova.tws.core.data.view.rememberSaveableWebViewState
import si.inova.tws.core.data.view.rememberWebViewNavigator
import si.inova.tws.core.util.initializeSettings

/**
 *
 * Extension of WebView
 *
 *
 * @param target A property, which contains data to show in WebView
 * @param modifier A compose modifier
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param webViewState State of WebView
 * @param displayErrorViewOnError Show custom error content
 * if there is error during loading WebView content
 * @param errorViewContent If [displayErrorViewOnError] is set to true
 * show this compose error content
 * @param displayPlaceholderWhileLoading Show custom loading animation while loading WebView content
 * @param loadingPlaceholderContent If [displayPlaceholderWhileLoading] is set to true
 * show this compose loading content
 * @param interceptOverrideUrl Optional callback, how to handle intercepted urls,
 * return true if do not want to navigate to the new url and
 * return false if navigation to the new url is intact
 * @param googleLoginRedirectUrl open new intent for google login url
 */
@Composable
fun WebSnippetComponent(
   target: WebSnippetData,
   modifier: Modifier = Modifier,
   navigator: WebViewNavigator = rememberWebViewNavigator(target.id),
   webViewState: WebViewState = rememberSaveableWebViewState(target.id),
   displayErrorViewOnError: Boolean = false,
   errorViewContent: @Composable () -> Unit = { ErrorViewIndicator() },
   displayPlaceholderWhileLoading: Boolean = false,
   loadingPlaceholderContent: @Composable () -> Unit = { WebViewLoadingIndicator() },
   interceptOverrideUrl: (String) -> Boolean = { false },
   googleLoginRedirectUrl: String? = null
) {
   LaunchedEffect(navigator, target.loadIteration) {
      if (webViewState.lastLoadedUrl == null || webViewState.loadIteration != target.loadIteration) {
         // This is the first time load, so load the home page.
         navigator.loadUrl(
            url = target.url,
            loadOnlyInitial = webViewState.loadIteration == null,
            additionalHttpHeaders = target.headers
         )

         webViewState.loadIteration = target.loadIteration
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
         if (popupStates.value.isEmpty() && data?.startsWith(googleLoginRedirectUrl) == true) {
            navigator.loadUrl(data, loadOnlyInitial = false)
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
      interceptOverrideUrl = interceptOverrideUrl,
      errorViewContent = errorViewContent,
      popupStateCallback = popupStateCallback,
      dynamicModifiers = target.dynamicModifiers
   )

   popupStates.value.forEach { state ->
      PopUpWebView(
         popupState = state,
         displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
         loadingPlaceholderContent = loadingPlaceholderContent,
         displayErrorViewOnError = displayErrorViewOnError,
         errorViewContent = errorViewContent,
         onDismissRequest = { popupStates.value = popupStates.value.filter { it != state } },
         popupStateCallback = popupStateCallback,
         interceptOverrideUrl = interceptOverrideUrl,
         googleLoginRedirectUrl = googleLoginRedirectUrl,
         dynamicModifiers = target.dynamicModifiers
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
   errorViewContent: @Composable () -> Unit,
   modifier: Modifier = Modifier,
   onCreated: (WebView) -> Unit = {},
   popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null,
   interceptOverrideUrl: (String) -> Boolean,
   dynamicModifiers: List<ModifierPageData>? = null
) {
   // https://github.com/google/accompanist/issues/1326 - WebView settings does not work in compose preview
   val isPreviewMode = LocalInspectionMode.current

   Box(modifier = modifier) {
      if (!displayLoadingContent && !displayErrorContent) {
         WebView(
            key = key,
            modifier = Modifier.fillMaxSize(),
            state = webViewState,
            navigator = navigator,
            onCreated = {
               if (!isPreviewMode) it.initializeSettings()
               onCreated(it)
            },
            popupStateCallback = popupStateCallback,
            interceptOverrideUrl = interceptOverrideUrl,
            dynamicModifiers = dynamicModifiers
         )
      }

      if (displayLoadingContent) {
         loadingPlaceholderContent()
      }

      if (displayErrorContent) {
         errorViewContent()
      }
   }
}

@Composable
private fun PopUpWebView(
   popupState: WebViewState,
   displayPlaceholderWhileLoading: Boolean,
   loadingPlaceholderContent: @Composable () -> Unit,
   displayErrorViewOnError: Boolean,
   errorViewContent: @Composable () -> Unit,
   onDismissRequest: () -> Unit,
   interceptOverrideUrl: (String) -> Boolean = { false },
   popupNavigator: WebViewNavigator = rememberWebViewNavigator(),
   popupStateCallback: ((WebViewState, Boolean) -> Unit)? = null,
   googleLoginRedirectUrl: String? = null,
   dynamicModifiers: List<ModifierPageData>? = null
) {
   val displayErrorContent = displayErrorViewOnError && popupState.hasError
   val displayLoadingContent =
      displayPlaceholderWhileLoading && popupState.loadingState is LoadingState.Loading

   googleLoginRedirectUrl?.let {
      NewIntentListener { intent ->
         val data = intent.data?.toString()
         if (data?.startsWith(googleLoginRedirectUrl) == true) {
            popupNavigator.loadUrl(data, loadOnlyInitial = false)
         }
      }
   }

   Dialog(
      onDismissRequest = {
         popupState.webView?.destroy()
         onDismissRequest()
      }
   ) {
      Surface(
         modifier = Modifier
            .fillMaxHeight(WEB_VIEW_POPUP_HEIGHT_PERCENTAGE)
            .fillMaxWidth(WEB_VIEW_POPUP_WIDTH_PERCENTAGE),
         shape = RoundedCornerShape(16.dp),
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
            onCreated = { webView ->
               val popupMessage =
                  popupState.popupMessage ?: return@SnippetContentWithLoadingAndError
               val transport = popupMessage.obj as? WebView.WebViewTransport
               if (transport != null) {
                  transport.webView = webView
                  popupMessage.sendToTarget()
               }
            },
            popupStateCallback = popupStateCallback,
            interceptOverrideUrl = interceptOverrideUrl,
            dynamicModifiers = dynamicModifiers
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

@Composable
private fun WebViewLoadingIndicator() {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .height(200.dp)
         .background(Color.LightGray)
   ) {
      CircularProgressIndicator(
         modifier = Modifier
            .size(64.dp)
            .align(Alignment.Center),
         color = MaterialTheme.colorScheme.secondary,
         trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
   }
}

@Composable
private fun ErrorViewIndicator() {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .height(200.dp)
         .background(Color.LightGray)
   ) {
      Column(modifier = Modifier.align(Alignment.Center)) {
         Image(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.image_load_failed),
            contentDescription = "Web view error image"
         )

         Text(
            modifier = Modifier.padding(all = 16.dp),
            text = stringResource(id = R.string.oops_loading_failed),
            style = TextStyle(color = Color.Black)
         )
      }
   }
}

private const val WEB_VIEW_POPUP_WIDTH_PERCENTAGE = 0.95f
private const val WEB_VIEW_POPUP_HEIGHT_PERCENTAGE = 0.8f

@Composable
@Preview
private fun WebSnippetComponentPreview() {
   WebSnippetComponent(WebSnippetData(id = "id", url = "https://www.google.com/"))
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderComponentPreview() {
   WebSnippetComponent(
      WebSnippetData(id = "id", url = "https://www.google.com/"),
      webViewState = webStateLoading,
      displayErrorViewOnError = true,
      displayPlaceholderWhileLoading = true
   )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderInitComponentPreview() {
   WebSnippetComponent(
      WebSnippetData(id = "id", url = "https://www.google.com/"),
      webViewState = webStateInitializing,
      displayErrorViewOnError = true,
      displayPlaceholderWhileLoading = true
   )
}

@Composable
@Preview
private fun WebSnippetLoadingPlaceholderFinishedComponentPreview() {
   WebSnippetComponent(
      WebSnippetData(id = "id", url = "https://www.google.com/"),
      webViewState = webStateLoadingFinished,
      displayErrorViewOnError = true,
      displayPlaceholderWhileLoading = true
   )
}

private val webStateInitializing =
   WebViewState(WebContent.NavigatorOnly)
      .apply { loadingState = LoadingState.Initializing }
private val webStateLoading =
   WebViewState(WebContent.NavigatorOnly)
      .apply { loadingState = LoadingState.Loading(0.5f) }
private val webStateLoadingFinished =
   WebViewState(WebContent.NavigatorOnly)
      .apply { loadingState = LoadingState.Finished }
