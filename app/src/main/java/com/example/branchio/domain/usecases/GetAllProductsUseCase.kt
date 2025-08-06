package com.example.branchio.domain.usecases

import com.example.branchio.data.models.ApiResult
import com.example.branchio.data.models.Product
import com.example.branchio.domain.repository.ProductRepository

class GetAllProductsUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke() : ApiResult<List<Product>> {
        return repository.getAllProducts()
    }
}