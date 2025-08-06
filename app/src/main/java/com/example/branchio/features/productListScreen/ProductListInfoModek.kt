package com.example.branchio.features.productListScreen

import com.example.branchio.data.models.Product

sealed class ProductListEvent {
    object LoadProducts : ProductListEvent()
    object RefreshProducts : ProductListEvent()
    data class OnProductClick(val productId: Int) : ProductListEvent()
    data class OnRetryClick(val productId: Int? = null) : ProductListEvent()
}

sealed class ProductListEffect {
    data class NavigateToProductDetail(val productId: Int) : ProductListEffect()
    data class ShowError(val message: String) : ProductListEffect()
    data class ShowSnackbar(val message: String) : ProductListEffect()
}

sealed class ProductListState {
    object Loading : ProductListState()
    data class Success(val products: List<Product>) : ProductListState()
    data class Error(val message: String) : ProductListState()
    object Empty : ProductListState()
}