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

package com.thewebsnippet.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thewebsnippet.sample.examples.injection.TWSViewInjectionExample
import com.thewebsnippet.sample.examples.mustache.TWSViewMustacheExample
import com.thewebsnippet.sample.examples.navigation.TWSViewCustomInterceptorExample
import com.thewebsnippet.sample.examples.permissions.TWSViewPermissionsExample
import com.thewebsnippet.sample.examples.tabs.TWSViewCustomTabsExample
import com.thewebsnippet.sample.ui.theme.SampleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.TWSViewCustomInterceptorExample.route) {
                    composable(Screen.TWSViewCustomInterceptorExample.route) { TWSViewCustomInterceptorExample(navController) }
                    composable(Screen.TWSViewCustomTabsExample.route) { TWSViewCustomTabsExample() }
                    composable(Screen.TWSViewMustacheExample.route) { TWSViewMustacheExample() }
                    composable(Screen.TWSViewInjectionExample.route) { TWSViewInjectionExample() }
                    composable(Screen.TWSViewPermissionsExample.route) { TWSViewPermissionsExample() }
                }
            }
        }
    }
}
