package com.example.closetconnect
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("products")
    suspend fun getProducts():List<Product>
    @GET("products/category/{categoryName}")
    suspend fun getInCategory(@Path("categoryName") categoryName: String): List<Product>
}