package com.example.branchio.core.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.branchio.features.deepLink.DeepLinkScreen
import com.example.branchio.features.deepLink.DeepLinkViewModel
import com.example.branchio.features.home.viewModels.HomeScreenViewModel
import com.example.branchio.features.presentation.home.HomeScreen
import org.koin.compose.koinInject

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            val homeVM: HomeScreenViewModel = koinInject()
            HomeScreen(navController, homeVM)
        }

        composable(
            route = Screen.DeepLink.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://n2ujk.test-app.link/{deeplink}"
                    action = Intent.ACTION_VIEW
                },
                navDeepLink {
                    uriPattern = "https://n2ujk-alternate.test-app.link/{deeplink}"
                    action = Intent.ACTION_VIEW
                }
            ),
            arguments = listOf(
                navArgument("deeplink") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            // Safely extract parameters and create intent
            val deepLinkPath = backStackEntry.arguments?.getString("deeplink") ?: ""
            val baseIntent = activity?.intent

            // Log for debugging
            Log.d("AppNavGraph", "Processing deep link path: $deepLinkPath")
            Log.d("AppNavGraph", "Base intent: $baseIntent")
            Log.d("AppNavGraph", "Base intent data: ${baseIntent?.data}")

            // Use remember to safely create intent once
            val safeIntent = remember(deepLinkPath, baseIntent) {
                val intent = Intent().apply {
                    // Set data - use existing or construct new
                    data = baseIntent?.data ?: run {
                        if (deepLinkPath.isNotEmpty()) {
                            Uri.parse("https://n2ujk.test-app.link/$deepLinkPath")
                        } else {
                            Uri.parse("https://n2ujk.test-app.link/")
                        }
                    }

                    // Add fallback path as extra
                    putExtra("deeplink", deepLinkPath)
                    putExtra("branch_force_new_session", true)

                    // Set action
                    action = baseIntent?.action ?: Intent.ACTION_VIEW

                    // Copy other relevant extras from base intent if they exist
                    baseIntent?.extras?.let { extras ->
                        putExtras(extras)
                    }
                }

                Log.d("AppNavGraph", "Created safe intent: $intent")
                Log.d("AppNavGraph", "Intent data: ${intent.data}")

                intent
            }

            // Validate if we have necessary components before proceeding
            if (activity != null && safeIntent.data != null) {
                val deepLinkVM: DeepLinkViewModel = koinInject()

                DeepLinkScreen(
                    navController = navController,
                    intent = safeIntent,
                    viewModel = deepLinkVM,
                    deepLinkPath = deepLinkPath
                )
            } else {
                // Fallback UI for invalid state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Text("Deep Link Error")
                        Text("Unable to process deep link")
                        if (activity == null) {
                            Text("Activity context not available")
                        }
                        if (safeIntent.data == null) {
                            Text("Invalid deep link data")
                        }
                    }
                }
            }
        }
    }
}
