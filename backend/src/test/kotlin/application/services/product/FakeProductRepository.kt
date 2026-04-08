package com.qhack.application.services.product

import com.qhack.application.domain.product.ProductData
import com.qhack.application.infrastructure.product.ProductRepository

class FakeProductRepository : ProductRepository {
    private var nextId = 1
    val productsList = mutableListOf<Pair<Int, ProductData>>()

    override suspend fun addProduct(data: ProductData): Int {
        val id = nextId++
        productsList.add(id to data)
        return id
    }

    override suspend fun getAllProducts(): List<Pair<Int, ProductData>> {
        return productsList
    }

    override suspend fun getProductById(id: Int): ProductData? {
        return productsList.find { it.first == id }?.second
    }

    override suspend fun exists(productId: Int): Boolean {
        return productsList.any { it.first == productId }
    }
}
