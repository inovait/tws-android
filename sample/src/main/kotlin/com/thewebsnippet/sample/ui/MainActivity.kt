package com.thewebsnippet.sample.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
				}
			}
		}
	}
}
