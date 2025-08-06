package com.example.branchio.features.productListScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.branchio.data.models.ApiResult
import com.example.branchio.domain.usecases.GetAllProductsUseCase
import com.example.branchio.domain.usecases.TrackCustomEventUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductListViewModel(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val trackCustomEventUseCase: TrackCustomEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ProductListState>(ProductListState.Loading)
    val state: StateFlow<ProductListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProductListEffect>()
    val effect: SharedFlow<ProductListEffect> = _effect.asSharedFlow()

    init {
        loadProducts()
    }

    fun onEvent(event: ProductListEvent) {
        when (event) {
            is ProductListEvent.LoadProducts -> loadProducts()
            is ProductListEvent.RefreshProducts -> refreshProducts()
            is ProductListEvent.OnProductClick -> {
                trackCustomEventUseCase(
                    eventName = "product_clicked",
                    customData = mapOf(
                        "product_id" to event.productId,
                        "source" to "product_list",
                        "timestamp" to System.currentTimeMillis()
                    )
                )

                viewModelScope.launch {
                    _effect.emit(ProductListEffect.NavigateToProductDetail(event.productId))
                }
            }
            is ProductListEvent.OnRetryClick -> loadProducts()
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = ProductListState.Loading

            when (val result = getAllProductsUseCase()) {
                is ApiResult.Success -> {
                    if (result.data.isEmpty()) {
                        _state.value = ProductListState.Empty
                    } else {
                        _state.value = ProductListState.Success(result.data)
                        Log.d("ProductListVM", "Loaded ${result.data.size} products")
                    }
                }
                is ApiResult.Error -> {
                    val errorMessage = result.exception.message ?: "Unknown error occurred"
                    _state.value = ProductListState.Error(errorMessage)
                    _effect.emit(ProductListEffect.ShowError(errorMessage))
                }
                is ApiResult.Loading -> {
                    _state.value = ProductListState.Loading
                }
            }
        }
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            when (val result = getAllProductsUseCase()) {
                is ApiResult.Success -> {
                    _state.value = ProductListState.Success(result.data)
                    _effect.emit(ProductListEffect.ShowSnackbar("Products refreshed"))
                }
                is ApiResult.Error -> {
                    val errorMessage = result.exception.message ?: "Failed to refresh"
                    _effect.emit(ProductListEffect.ShowError(errorMessage))
                }
                is ApiResult.Loading -> {
                    // Keep current state during refresh
                }
            }
        }
    }
}
