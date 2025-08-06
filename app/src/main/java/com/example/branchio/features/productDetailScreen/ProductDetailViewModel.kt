package com.example.branchio.features.productDetailScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.data.models.ApiResult
import com.example.branchio.data.models.Product
import com.example.branchio.domain.usecases.GenerateBranchLinkUseCase
import com.example.branchio.domain.usecases.GetProductByIdUseCase
import com.example.branchio.domain.usecases.TrackContentViewUseCase
import com.example.branchio.domain.usecases.TrackCustomEventUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val generateBranchLinkUseCase: GenerateBranchLinkUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val trackCustomEventUseCase: TrackCustomEventUseCase,
    private val trackContentViewUseCase: TrackContentViewUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProductDetailEffect>()
    val effect: SharedFlow<ProductDetailEffect> = _effect.asSharedFlow()

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            is ProductDetailEvent.LoadProduct -> loadProduct(event.productId)
            is ProductDetailEvent.ShareProduct -> shareProduct(event.product)
            is ProductDetailEvent.RetryLoading -> {
                val currentState = _state.value
                if (currentState is ProductDetailState.Error) {
                    _state.value = ProductDetailState.Loading
                }
            }
        }
    }

    private fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailState.Loading

            when (val result = getProductByIdUseCase(productId)) {
                is ApiResult.Success -> {
                    _state.value = ProductDetailState.Success(result.data)
                    Log.d("ProductDetailVM", "Product loaded: ${result.data.title}")
                    trackContentViewUseCase(result.data)
                }
                is ApiResult.Error -> {
                    val errorMessage = result.exception.message ?: "Failed to load product"
                    _state.value = ProductDetailState.Error(errorMessage)
                    _effect.emit(ProductDetailEffect.ShowSnackbar(errorMessage))
                }
                is ApiResult.Loading -> {
                    _state.value = ProductDetailState.Loading
                }
            }
        }
    }

    private fun shareProduct(product: Product) {
        viewModelScope.launch {
            try {
                val result = generateBranchLinkUseCase(product)

                result.fold(
                    onSuccess = { url ->
                        trackCustomEventUseCase(
                            eventName = "share_link_generated",
                            product = product,
                            customData = mapOf("link_url" to url)
                        )
                        _effect.emit(ProductDetailEffect.ShareLink(url))

                        _effect.emit(ProductDetailEffect.ShowSnackbar("Share link generated!"))
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: "Failed to generate share link"
                        trackCustomEventUseCase(
                            eventName = "share_link_failed",
                            product = product,
                            customData = mapOf("error" to (error.message ?: "Unknown"))
                        )
                        _effect.emit(ProductDetailEffect.ShowSnackbar(errorMessage))
                    }
                )

            } catch (e: Exception) {
                Log.e("ProductDetailVM", "Error sharing product", e)
                _effect.emit(ProductDetailEffect.ShowSnackbar("Error generating share link"))
            }
        }
    }

}
