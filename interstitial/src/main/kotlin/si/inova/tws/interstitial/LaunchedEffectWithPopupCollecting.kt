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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import si.inova.tws.manager.WebSnippetManagerImpl

@Composable
fun LaunchedEffectWithPopupCollecting(managerTag: String?) {
    val owner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(managerTag ?: Unit) {
        owner.launchPopupCollecting(context, managerTag)
    }
}

fun LifecycleOwner.launchPopupCollecting(context: Context, managerTag: String? = null) {
    lifecycleScope.launch {
        val sharedManager = WebSnippetManagerImpl.getSharedInstance(context, managerTag)
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            sharedManager.unseenPopupSnippetsFlow.collect { newPopups ->
                if (newPopups.isNotEmpty()) {
                    val ids = newPopups.map { it.id }
                    WebSnippetPopup.open(context, ids, managerTag)
                    sharedManager.markPopupsAsSeen(ids)
                }
            }
        }
    }
}
