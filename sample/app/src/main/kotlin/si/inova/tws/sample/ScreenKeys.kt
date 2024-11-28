package si.inova.tws.sample

/**
 * A sealed class used for navigating between screens.
 * @param route The name of the screen.
 */
sealed class Screen(val route: String) {
    data object TWSViewCustomInterceptorExample: Screen("customInterceptorExample")
    data object TWSViewCustomTabsExample : Screen("customTabsExample")
    data object TWSViewMustacheExample : Screen("mustacheExample")
    data object TWSViewInjectionExample : Screen("injectionExample")
    data object TWSViewPermissionsExample : Screen("permissionsExample")
}
