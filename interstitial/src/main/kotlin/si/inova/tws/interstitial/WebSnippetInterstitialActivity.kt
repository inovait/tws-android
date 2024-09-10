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

package si.inova.tws.interstitial

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.tws.core.WebSnippetComponent
import si.inova.tws.core.data.ModifierInjectionType
import si.inova.tws.core.data.UrlInjectData
import si.inova.tws.core.data.WebSnippetData
import si.inova.tws.interstitial.WebSnippetPopup.Companion.MANAGER_TAG
import si.inova.tws.interstitial.WebSnippetPopup.Companion.WEB_SNIPPET_DATA
import si.inova.tws.interstitial.WebSnippetPopup.Companion.WEB_SNIPPET_ID
import si.inova.tws.manager.WebSnippetManagerImpl
import si.inova.tws.manager.data.WebSnippetDto

class WebSnippetInterstitialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // For dark (black) colors
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        val webSnippetId = intent.getStringExtra(WEB_SNIPPET_ID)
        val managerTag = intent.getStringExtra(MANAGER_TAG)

        val manager = webSnippetId?.let {
            WebSnippetManagerImpl.getSharedInstance(this, managerTag)
        }

        val webSnippetData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(WEB_SNIPPET_DATA, WebSnippetData::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(WEB_SNIPPET_DATA)
        }

        setContent {
            val shouldCloseFlow = manager?.popupSnippetsFlow?.map { outcome ->
                outcome is Outcome.Success && !outcome.data.any {
                    it.id == webSnippetId
                }
            }?.collectAsState(false)?.value ?: false

            val snippet = manager?.popupSnippetsFlow?.map { outcome ->
                outcome.data?.find { it.id == webSnippetId }?.toWebSnippetData()
            }?.filterNotNull()?.collectAsState(null)?.value ?: webSnippetData

            // State to manage animation visibility, allows us to display web
            // snippet component only when animation is finished, to avoid flashing
            val transitionState = remember {
                MutableTransitionState(false).apply {
                    targetState = true
                }
            }

            // Start finish animation if snippet was removed from manager
            LaunchedEffect(shouldCloseFlow) {
                if (shouldCloseFlow) {
                    transitionState.targetState = false
                }
            }

            // Detect when the exit animation completes and finish the activity
            LaunchedEffect(transitionState) {
                snapshotFlow { transitionState.isIdle && !transitionState.currentState }.collect { shouldFinish ->
                    if (shouldFinish) {
                        finish()
                    }
                }
            }

            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    snippet?.let {
                        if (transitionState.currentState) {
                            WebSnippetComponent(
                                modifier = Modifier.fillMaxSize(),
                                target = it,
                                displayPlaceholderWhileLoading = true
                            )
                        }

                        FilledIconButton(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopEnd)
                                .alpha(0.7f),
                            onClick = { transitionState.targetState = false }
                        ) {
                            Icon(Icons.Default.Close, "close")
                        }
                    }
                }
            }
        }
    }
}

private fun WebSnippetDto.toWebSnippetData(): WebSnippetData {
    return WebSnippetData(
        id = id,
        url = target,
        headers = headers.orEmpty(),
        loadIteration = loadIteration,
        dynamicModifiers = dynamicResources?.map {
            UrlInjectData(
                it.url,
                ModifierInjectionType.fromContentType(it.contentType)
            )
        }.orEmpty()
    )
}
