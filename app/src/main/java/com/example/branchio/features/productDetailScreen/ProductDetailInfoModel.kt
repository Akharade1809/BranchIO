package com.example.branchio.features.productDetailScreen

import com.example.branchio.data.models.Product

sealed class ProductDetailEvent {
    data class LoadProduct(val productId: Int) : ProductDetailEvent()
    data class ShareProduct(val product: Product) : ProductDetailEvent()
    object RetryLoading : ProductDetailEvent()
}

sealed class ProductDetailEffect {
    data class ShowSnackbar(val message: String) : ProductDetailEffect()
    data class ShareLink(val url: String) : ProductDetailEffect()
    object NavigateBack : ProductDetailEffect()
}

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}
