package com.example.branchio.core.navigation

sealed class Screen(val route: String) {
    object ProductList : Screen("product_list")
    object ProductDetail : Screen("product_detail/{productId}"){
        fun createRoute(productId: String): String {
            return "product_detail/$productId"
        }
    }
}