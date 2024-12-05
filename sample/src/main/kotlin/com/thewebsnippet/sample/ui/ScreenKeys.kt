package com.thewebsnippet.sample.ui

/**
 * A sealed class used for navigating between screens.
 * @param route The name of the screen.
 */
sealed class Screen(val route: String) {
    data object TWSViewCustomInterceptorExampleKey : Screen("customInterceptorExample")
    data object TWSViewCustomTabsExampleKey : Screen("customTabsExample")
    data object TWSViewMustacheExampleKey : Screen("mustacheExample")
    data object TWSViewInjectionExampleKey : Screen("injectionExample")
    data object TWSViewPermissionsExampleKey : Screen("permissionsExample")
}
