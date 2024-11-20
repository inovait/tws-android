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

package si.inova.tws.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun NavigationScreen(
    navController: NavController
) {
    Scaffold(
        modifier = Modifier
            .padding(8.dp),
        topBar = { TopBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
        ) {
            item {
                NavigationItem(
                    title = "Tab names and icons",
                    description = "In this example you will learn about how you can set your own custom "
                            + "tab names and tab icons to your content"
                ) { navController.navigate(Screen.Example1Screen.route) }
            }
            item {
                NavigationItem(
                    title = "Mustache",
                    description = "In this example you will see how to use mustache processing "
                            + "to dynamically set content to existing html"
                ) {}
            }
            item {
                NavigationItem(
                    title = "Injection",
                    description = "In this example we will showcase how you can inject CSS and javascript "
                    + "in your html"
                ) {}
            }
            item {
                NavigationItem(
                    title = "Login redirection",
                    description = "In this example we will showcase how to handle login redirects "
                    + "that can be handled natively"
                ) {}
            }
            item {
                NavigationItem(
                    title = "Intercepts",
                    description = "In this example you will learn how to create custom intercepts "
                    + "to url requests"
                ) {}
            }
        }
    }
}

@Composable
private fun NavigationItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier
        .padding(4.dp)
        .clickable { onClick() }
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            TitleText(text = title)
            DescriptionText(text = description)
        }
    }
}

@Composable
private fun TitleText(text: String) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = text,
        minLines = 1,
        textAlign = TextAlign.Justify,
        style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp)
    )
}

@Composable
private fun DescriptionText(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 8.dp),
        text = text,
        textAlign = TextAlign.Justify,
        minLines = 3,
        maxLines = 3,
        style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
    )
}

@Composable
private fun TopBar() {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.tws_logo),
            contentDescription = stringResource(R.string.logo_icon),
            modifier = Modifier.size(48.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "The Web Snippet Examples", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
    }
}








