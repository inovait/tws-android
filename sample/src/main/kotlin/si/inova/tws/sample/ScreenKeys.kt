package si.inova.tws.sample

sealed class Screen(val route: String) {
    data object TWSViewCustomInterceptorExample: Screen("customInterceptorExample")
    data object TWSViewCustomTabsExample : Screen("customTabsExample")
    data object TWSViewMustacheExample : Screen("mustacheExample")
    data object TWSViewInjectionExample : Screen("injectionExample")
    data object TWSViewLoginRedirectExample : Screen("loginRedirectExample")
}