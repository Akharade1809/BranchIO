package com.example.branchio.core.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.branchio.core.viewmodel.AppEffect
import com.example.branchio.core.viewmodel.AppViewModel
import com.example.branchio.features.productDetailScreen.ProductDetailScreen
import com.example.branchio.features.productDetailScreen.ProductDetailViewModel
import com.example.branchio.features.productListScreen.ProductListScreen
import com.example.branchio.features.productListScreen.ProductListViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavGraph(appViewModel: AppViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        appViewModel.navigationEvents.collect { effect ->
            Log.d("AppNavGraph", " RECEIVED NAVIGATION EVENT: $effect")

            when (effect) {
                is AppEffect.NavigateToProductDetail -> {
                    Log.d("AppNavGraph", " PROCESSING NAVIGATION: Product Detail ${effect.productId}")

                    try {
                        val route = Screen.ProductDetail.createRoute(effect.productId.toString())
                        Log.d("AppNavGraph", " Attempting to navigate to route: $route")

                        navController.navigate(route) {
                            popUpTo(Screen.ProductList.route) {
                                inclusive = false
                            }
                        }
                        Log.d("AppNavGraph", " Navigation command executed successfully!")

                    } catch (e: Exception) {
                        Log.e("AppNavGraph", " Navigation failed with exception", e)
                    }
                }
                is AppEffect.ShowError -> {
                    Log.e("AppNavGraph", " Navigation error: ${effect.message}")
                }

                AppEffect.NavigateToHome -> {}
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.ProductList.route
    ) {
        composable(route = Screen.ProductList.route) {
            Log.d("AppNavGraph", "ProductListScreen displayed")
            val productListVM: ProductListViewModel = koinInject()
            ProductListScreen(
                navController = navController,
                viewModel = productListVM
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            Log.d("AppNavGraph", " ProductDetailScreen displayed with ID: $productId")

            val productDetailVM: ProductDetailViewModel = koinInject()
            ProductDetailScreen(
                productId = productId,
                navController = navController,
                viewModel = productDetailVM
            )
        }
    }
}

