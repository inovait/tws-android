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

package si.inova.tws.sample.examples.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import si.inova.tws.core.TWSView
import si.inova.tws.manager.TWSConfiguration
import si.inova.tws.manager.TWSFactory
import si.inova.tws.manager.TWSManager
import si.inova.tws.manager.TWSOutcome
import si.inova.tws.manager.mapData
import si.inova.tws.sample.R
import si.inova.tws.sample.components.ErrorView
import si.inova.tws.sample.components.LoadingView

@Composable
fun TWSViewPermissionsExample() {
    val context = LocalContext.current
    val manager: TWSManager = remember(Unit) { TWSFactory.get(context, config) }

    val permissionSnippetState = manager.snippets.collectAsStateWithLifecycle(null).value?.mapData { state ->
        state.firstOrNull { it.id == "permissions" }
    } ?: return

    val snippet = permissionSnippetState.data
    when {
        snippet != null -> {
            TWSView(snippet = snippet)
        }

        permissionSnippetState is TWSOutcome.Error -> {
            ErrorView(permissionSnippetState.exception.message ?: stringResource(R.string.error_message))
        }

        permissionSnippetState is TWSOutcome.Progress -> {
            LoadingView()
        }
    }
}

private const val organizationId = "examples"
private const val projectId = "example4"
private val config = TWSConfiguration.Basic(organizationId, projectId, "apiKey")
