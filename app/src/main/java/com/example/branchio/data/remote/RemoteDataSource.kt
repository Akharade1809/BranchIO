package com.example.branchio.data.remote

import android.util.Log
import com.example.branchio.data.models.ApiResult
import com.example.branchio.data.models.Product
import com.google.android.gms.common.api.Api
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RemoteDataSource {

    suspend fun getAllProducts() : ApiResult<List<Product>>

    suspend fun getProductById(id : Int) : ApiResult<Product>
}

class RemoteDataSourceImpl(
    private val httpClient : HttpClient = HttpClientFactory.create()
) : RemoteDataSource{

    companion object {
        private const val BASE_URL = "https://fakestoreapi.com"
        private const val PRODUCTS_ENDPOINT = "$BASE_URL/products"
        private const val TAG = "RemoteDataSource"
    }

    override suspend fun getAllProducts(): ApiResult<List<Product>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching products from: $PRODUCTS_ENDPOINT")

                val response = httpClient.get(PRODUCTS_ENDPOINT) {
                    headers {
                        append(HttpHeaders.Accept, "application/json")
                    }
                }

                if (response.status == HttpStatusCode.OK) {
                    val products: List<Product> = response.body()
                    Log.d(TAG, "Successfully fetched ${products.size} products")
                    ApiResult.Success(products)
                } else {
                    val error = Exception("HTTP ${response.status.value}: ${response.status.description}")
                    Log.e(TAG, "API call failed", error)
                    ApiResult.Error(error)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Network request failed", e)
                ApiResult.Error(e)
            }
        }
    }

    override suspend fun getProductById(id: Int): ApiResult<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$PRODUCTS_ENDPOINT/$id"
                Log.d(TAG, "Fetching product with ID: $id from: $url")

                val response = httpClient.get(url) {
                    headers {
                        append(HttpHeaders.Accept, "application/json")
                    }
                }

                if (response.status == HttpStatusCode.OK) {
                    val product: Product = response.body()
                    Log.d(TAG, "Successfully fetched product: ${product.title}")
                    ApiResult.Success(product)
                } else {
                    val error = Exception("HTTP ${response.status.value}: ${response.status.description}")
                    Log.e(TAG, "API call failed for product ID: $id", error)
                    ApiResult.Error(error)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Network request failed for product ID: $id", e)
                ApiResult.Error(e)
            }
        }
    }
}