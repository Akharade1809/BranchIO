package com.example.branchio.domain.repository

import com.example.branchio.data.models.ApiResult
import com.example.branchio.data.models.Product
import com.example.branchio.data.remote.RemoteDataSource

interface ProductRepository{

    suspend fun getAllProducts(): ApiResult<List<Product>>
    suspend fun getProductById(id: Int): ApiResult<Product>
}

class ProductRepositoryImpl(
    private val remoteDataSource: RemoteDataSource
) : ProductRepository{
    override suspend fun getAllProducts(): ApiResult<List<Product>> {
        return remoteDataSource.getAllProducts()
    }

    override suspend fun getProductById(id: Int): ApiResult<Product> {
        return remoteDataSource.getProductById(id)
    }
}