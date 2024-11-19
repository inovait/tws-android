package si.inova.tws.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import si.inova.tws.example.exampleScreen.Example1Screen
import si.inova.tws.example.ui.theme.Example1Theme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Example1Theme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.NavigationScreen.route) {
                    composable(Screen.NavigationScreen.route) { NavigationScreen(navController) }
                    composable(Screen.Example1Screen.route) { Example1Screen() }
                    composable(Screen.Example2Screen.route) {  }
                    composable(Screen.Example3Screen.route) {  }
                    composable(Screen.Example4Screen.route) {  }
                    composable(Screen.Example5Screen.route) {  }
                }
            }
        }
    }
}
