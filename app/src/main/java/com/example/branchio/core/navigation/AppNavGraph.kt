package com.example.branchio.core.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.branchio.features.deepLink.DeepLinkScreen
import com.example.branchio.features.home.viewModels.HomeScreenViewModel
import com.example.branchio.features.presentation.home.HomeScreen
import org.koin.compose.koinInject


@Composable
fun AppNavGraph() {
    val navController = rememberNavController();
    val context = LocalContext.current
    val activity = context as? Activity

    NavHost(navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            val homeVM : HomeScreenViewModel = koinInject()
            HomeScreen(navController, homeVM)
        }

        composable(
            route = Screen.DeepLink.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://n2ujk.test-app.link/deeplink"
                },
                navDeepLink {
                    uriPattern = "https://n2ujk-alternate.test-app.link/deeplink"
                }
            )
        ) { backStackEntry ->
            val intent = activity?.intent ?: Intent()

            DeepLinkScreen(
                navController = navController,
                intent = intent
            )
        }
    }
}