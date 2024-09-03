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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.collections.immutable.ImmutableList
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.core.util.compose.SnippetErrorView
import si.inova.tws.core.util.compose.SnippetLoadingView

@Composable
fun PopupSnippetComponent(
    targetsPopup: ImmutableList<WebSnippetData>?,
    displayErrorViewOnError: Boolean = false,
    errorViewContent: @Composable () -> Unit = { SnippetErrorView(true) },
    displayPlaceholderWhileLoading: Boolean = true,
    loadingPlaceholderContent: @Composable () -> Unit = { SnippetLoadingView(true) },
) {
    val showInterstitial = remember {
        mutableStateListOf<Pair<WebSnippetData, Boolean>>().apply {
            targetsPopup?.forEach { add(Pair(it, true)) }
        }
    }

    showInterstitial.forEachIndexed { i, data ->
        if (data.second) {
            val showAnimatedContent = remember { mutableStateOf(false) }

            Dialog(
                properties = DialogProperties(usePlatformDefaultWidth = false),
                onDismissRequest = {
                    showAnimatedContent.value = false
                }
            ) {
                val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
                dialogWindowProvider?.window?.setDimAmount(0f)

                LaunchedEffect(Unit) {
                    showAnimatedContent.value = true
                }

                AnimatedVisibility(
                    visible = showAnimatedContent.value,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                    )
                ) {

                    DisposableEffect(Unit) {
                        onDispose {
                            showInterstitial[i] = showInterstitial[i].copy(second = false)
                        }
                    }
                    Surface(modifier = Modifier.fillMaxSize()) {
                        WebSnippetComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            target = data.first,
                            displayErrorViewOnError = displayErrorViewOnError,
                            errorViewContent = errorViewContent,
                            displayPlaceholderWhileLoading = displayPlaceholderWhileLoading,
                            loadingPlaceholderContent = loadingPlaceholderContent
                        )
                    }
                }
            }
        }
    }
}
