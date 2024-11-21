package si.inova.tws.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import si.inova.tws.example.exampleScreen.TWSViewCustomTabsExample
import si.inova.tws.sample.exampleScreen.TWSViewInjectionExample
import si.inova.tws.sample.exampleScreen.TWSViewMustacheExample
import si.inova.tws.sample.ui.theme.TheWebSnippetSdkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheWebSnippetSdkTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.NavigationScreen.route) {
                    composable(Screen.NavigationScreen.route) { NavigationScreen(navController) }
                    composable(Screen.TWSViewCustomTabsExample.route) { TWSViewCustomTabsExample() }
                    composable(Screen.TWSViewMustacheExample.route) { TWSViewMustacheExample() }
                    composable(Screen.TWSViewInjectionExample.route) { TWSViewInjectionExample() }
                    composable(Screen.TWSViewLoginRedirectExample.route) { }
                    composable(Screen.TWSViewInterceptExample.route) { }
                }
            }
        }
    }
}
