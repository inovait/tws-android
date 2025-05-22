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
package com.thewebsnippet.sample.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewebsnippet.sample.R
import com.thewebsnippet.sample.ui.theme.SampleTheme

/**
 * A composable function that displays exclamation icon and a custom error message.
 *
 * @param errorText Custom text displayed when an error occurs.
 * @param modifier A [Modifier] to configure the layout or styling of the error view.
 * @sample com.thewebsnippet.sample.components.ErrorView
 */
@Composable
internal fun ErrorView(
    errorText: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    refresh: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(256.dp),
            painter = painterResource(R.drawable.ic_error_24),
            contentDescription = "Error"
        )
        Spacer(Modifier.size(8.dp))

        Text(
            text = errorText,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center
        )

        refresh?.let {
            Spacer(Modifier.size(8.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                onClick = refresh
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.error_try_again)
                )
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = stringResource(R.string.error_try_again)
                )
            }
        }
    }
}

@Composable
@Preview
private fun FullScreenErrorViewPreview() {
    SampleTheme {
        ErrorView("Sorry, something went wrong!")
    }
}

@Composable
@Preview
private fun FullScreenErrorViewRefreshPreview() {
    SampleTheme {
        ErrorView("Sorry, something went wrong!", refresh = {})
    }
}
