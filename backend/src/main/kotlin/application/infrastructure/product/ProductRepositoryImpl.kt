package com.qhack.application.infrastructure.product

import com.qhack.application.domain.product.ProductData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ProductRepositoryImpl : ProductRepository, BaseRepository() {

    override suspend fun addProduct(data: ProductData): Int = dbQuery {
        ProductTable.insertAndGetId {
            it[name] = data.name
        }.value
    }

    override suspend fun getAllProducts(): List<Pair<Int, ProductData>> = dbQuery {
        ProductTable.selectAll().map {
            it[ProductTable.id].value to ProductData(
                name = it[ProductTable.name]
            )
        }
    }

    override suspend fun getProductById(id: Int): ProductData? = dbQuery {
        ProductTable.selectAll()
            .where { ProductTable.id eq id }
            .map {
                ProductData(name = it[ProductTable.name])
            }.singleOrNull()
    }

    override suspend fun exists(id: Int): Boolean = dbQuery {
        !ProductTable.selectAll().where { ProductTable.id eq id }.empty()
    }
}
