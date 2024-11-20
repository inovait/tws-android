package si.inova.tws.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import si.inova.tws.example.exampleScreen.Example1Screen
import si.inova.tws.sample.exampleScreen.Example2Screen
import si.inova.tws.sample.exampleScreen.Example3Screen
import si.inova.tws.sample.ui.theme.TheWebSnippetSdkTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheWebSnippetSdkTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.NavigationScreen.route) {
                    composable(Screen.NavigationScreen.route) { NavigationScreen(navController) }
                    composable(Screen.Example1Screen.route) { Example1Screen() }
                    composable(Screen.Example2Screen.route) { Example2Screen() }
                    composable(Screen.Example3Screen.route) { Example3Screen() }
                    composable(Screen.Example4Screen.route) {  }
                    composable(Screen.Example5Screen.route) {  }
                }
            }
        }
    }
}
