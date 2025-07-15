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
package com.thewebsnippet.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thewebsnippet.sample.NativeViewUserEngagementExample
import com.thewebsnippet.sample.TWSViewCustomInterceptorExample
import com.thewebsnippet.sample.TWSViewCustomTabsExample
import com.thewebsnippet.sample.TWSViewInjectionExample
import com.thewebsnippet.sample.TWSViewMustacheExample
import com.thewebsnippet.sample.TWSViewPermissionsExample
import com.thewebsnippet.sample.ui.theme.SampleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			SampleTheme {
				val navController = rememberNavController()

				NavHost(
					navController = navController,
					startDestination = Screen.TWSViewCustomInterceptorExampleKey.route
				) {

					composable(Screen.TWSViewCustomInterceptorExampleKey.route) {
						TWSViewCustomInterceptorExample(navController)
					}
					composable(Screen.TWSViewCustomTabsExampleKey.route) {
						TWSViewCustomTabsExample()
					}
					composable(Screen.TWSViewMustacheExampleKey.route) {
						TWSViewMustacheExample()
					}
					composable(Screen.TWSViewInjectionExampleKey.route) {
						TWSViewInjectionExample()
					}
					composable(Screen.TWSViewPermissionsExampleKey.route) {
						TWSViewPermissionsExample()
					}
					composable(Screen.NativeViewUserEngagementExampleKey.route) {
						NativeViewUserEngagementExample()
					}
				}
			}
		}
	}
}
