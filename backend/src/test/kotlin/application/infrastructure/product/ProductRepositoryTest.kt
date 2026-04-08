package com.qhack.application.infrastructure.product

import com.qhack.application.domain.product.ProductData
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProductRepositoryTest {

    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.drop(ProductTable)
            SchemaUtils.create(ProductTable)
        }
        repository = ProductRepositoryImpl()
    }

    @Test
    fun `addProduct should insert a product and return its id`() = runBlocking {
        val product = ProductData("Test Product")
        val id = repository.addProduct(product)
        
        val allProducts = repository.getAllProducts()
        assertEquals(1, allProducts.size)
        assertEquals("Test Product", allProducts[0].second.name)
        assertEquals(id, allProducts[0].first)
    }

    @Test
    fun `getProductById should return the correct product`() = runBlocking {
        val product = ProductData("Unique Product")
        val id = repository.addProduct(product)
        
        val retrieved = repository.getProductById(id)
        assertNotNull(retrieved)
        assertEquals("Unique Product", retrieved.name)
    }
}
