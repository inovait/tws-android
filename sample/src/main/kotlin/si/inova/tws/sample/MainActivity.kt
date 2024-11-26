package si.inova.tws.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import si.inova.tws.sample.examples.injection.TWSViewInjectionExample
import si.inova.tws.sample.examples.mustache.TWSViewMustacheExample
import si.inova.tws.sample.examples.navigation.TWSViewCustomInterceptorExample
import si.inova.tws.sample.examples.permissions.TWSViewPermissionsExample
import si.inova.tws.sample.examples.tabs.TWSViewCustomTabsExample
import si.inova.tws.sample.ui.theme.SampleTheme

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
