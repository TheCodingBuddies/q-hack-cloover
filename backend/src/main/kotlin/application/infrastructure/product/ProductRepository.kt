package com.qhack.application.infrastructure.product

import com.qhack.application.domain.product.ProductData

interface ProductRepository {
    suspend fun addProduct(data: ProductData): Int
    suspend fun getAllProducts(): List<Pair<Int, ProductData>>
    suspend fun getProductById(id: Int): ProductData?
    suspend fun exists(id: Int): Boolean
}
