package si.inova.tws.sample

sealed class Screen(val route: String) {
    data object NavigationScreen: Screen("navigationScreen")
    data object Example1Screen: Screen("example1")
    data object Example2Screen: Screen("example2")
    data object Example3Screen: Screen("example3")
    data object Example4Screen: Screen("example4")
    data object Example5Screen: Screen("example5")
}