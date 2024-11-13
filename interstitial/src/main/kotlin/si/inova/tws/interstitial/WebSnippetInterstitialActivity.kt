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

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.tws.core.TWSView
import si.inova.tws.data.TWSSnippet
import si.inova.tws.interstitial.WebSnippetPopup.Companion.MANAGER_TAG
import si.inova.tws.interstitial.WebSnippetPopup.Companion.NAVIGATION_BAR_COLOR
import si.inova.tws.interstitial.WebSnippetPopup.Companion.STATUS_BAR_COLOR
import si.inova.tws.interstitial.WebSnippetPopup.Companion.WEB_SNIPPET_DATA
import si.inova.tws.interstitial.WebSnippetPopup.Companion.WEB_SNIPPET_ID
import si.inova.tws.manager.TWSFactory

/**
 * WebSnippetInterstitialActivity is a ComponentActivity responsible for displaying
 * a WebSnippet within an interstitial layout. It manages WebView content and configuration
 * through WebSnippetComponent and provides functionality to close the interstitial.
 *
 * Key features:
 * - Sets up the status and navigation bar colors based on provided extras.
 * - Can load and display a WebSnippet based on `webSnippetId`. In that case it also Listens for changes in the associated
 * WebSnippetManager and responds accordingly (i.e. closes activity when deleted or updates content when changed).
 * - Can load and display a WebSnippet from WebSnippetData. In that case, connection with manager is not established, meaning
 * changes to snippet will not be reflected.
 */
class WebSnippetInterstitialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val statusColor = intent.getStringExtra(STATUS_BAR_COLOR)
        val navigationColor = intent.getStringExtra(NAVIGATION_BAR_COLOR)

        if (statusColor != null) {
            window.statusBarColor = Color.parseColor(statusColor)
        }

        if (navigationColor != null) {
            window.navigationBarColor = Color.parseColor(navigationColor)
        }

        val webSnippetId = intent.getStringExtra(WEB_SNIPPET_ID)
        val managerTag = intent.getStringExtra(MANAGER_TAG)

        val manager = webSnippetId?.let {
            managerTag?.let { tag ->
                TWSFactory.get(tag)
            }
        }

        val webSnippetData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(WEB_SNIPPET_DATA, TWSSnippet::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(WEB_SNIPPET_DATA)
        }

        setContent {
            val shouldCloseFlow = manager?.snippetsFlow?.map { outcome ->
                outcome is Outcome.Success && !outcome.data.any {
                    it.id == webSnippetId
                }
            }?.collectAsState(false)?.value

            val snippet = manager?.snippetsFlow?.map { outcome ->
                outcome.data?.find { it.id == webSnippetId }
            }?.filterNotNull()?.collectAsState(null)?.value ?: webSnippetData

            LaunchedEffect(shouldCloseFlow) {
                if (shouldCloseFlow == true) {
                    finish()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                snippet?.let {
                    TWSView(
                        modifier = Modifier.fillMaxSize(),
                        snippet = it
                    )

                    FilledIconButton(
                        modifier = Modifier.padding(8.dp).align(Alignment.TopEnd).alpha(closeIconAlpha),
                        onClick = { finish() }
                    ) {
                        Icon(Icons.Default.Close, "close")
                    }
                }
            }
        }
    }

    private val closeIconAlpha: Float = 0.7f
}
