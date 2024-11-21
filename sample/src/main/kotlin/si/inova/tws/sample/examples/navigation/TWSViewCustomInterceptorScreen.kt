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

package si.inova.tws.sample.examples.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import si.inova.tws.core.TWSView
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager
import si.inova.tws.sample.components.LoadingView
import si.inova.tws.sample.components.ErrorView

@Composable
fun TWSViewCustomInterceptorScreen(navController: NavController) {
    val context = LocalContext.current

    // TWSFactory will take configuration from Android Manifest
    val manager: TWSManager = remember(Unit) { TWSFactory.get(context) }

    // Collecting TWSManager.snippets(), which returns list of available snippets, either cached
    // or up to date. Iff you want to get the current state, collect TWSManager.snippets, which
    // exposes TWSOutcome.Error, TWSOutcome.Progress or TWSOutcome.Success state
    val homeSnippet = manager.snippets().collectAsStateWithLifecycle(null).value?.firstOrNull {
        it.id == "UseCaseExample5-intercepts" // this should be changed to more user friendly message
    }

    homeSnippet?.let { home ->
        TWSView(
            snippet = home,
            loadingPlaceholderContent = { LoadingView() },
            errorViewContent = { ErrorView(it) },
            // Handling urls natively
            interceptUrlCallback = TWSViewDemoInterceptor { navController.navigate(it) }
        )
    }
}
