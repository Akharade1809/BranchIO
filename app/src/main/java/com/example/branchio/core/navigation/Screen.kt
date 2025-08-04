package com.example.branchio.core.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DeepLink : Screen("deeplink/{deeplink}") {
        fun createRoute(deeplink: String): String {
            return "deeplink/$deeplink"
        }
    }
    // Add more screens here as needed, e.g., object Detail : Screen("detail/{id}")
}