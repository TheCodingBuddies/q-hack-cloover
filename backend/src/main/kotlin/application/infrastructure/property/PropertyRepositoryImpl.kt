package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class PropertyRepositoryImpl : PropertyRepository, BaseRepository() {
    override suspend fun addProperty(data: PropertyData, score: Int, text: String): Int = dbQuery {
        PropertyTable.insertAndGetId {
            it[postCode] = data.postCode
            it[street] = data.street
            it[city] = data.city
            it[houseNumber] = data.houseNumber
            it[customerId] = data.customerId
            it[sunnyScore] = score
            it[explanation] = text
        }.value
    }

    override suspend fun getPropertiesByCustomerId(customerId: Int): List<PropertyData> = dbQuery {
        PropertyTable.selectAll()
            .where { PropertyTable.customerId eq customerId }
            .map {
                PropertyData(
                    postCode = it[PropertyTable.postCode],
                    street = it[PropertyTable.street],
                    city = it[PropertyTable.city],
                    houseNumber = it[PropertyTable.houseNumber],
                    customerId = it[PropertyTable.customerId].value,
                    sunnyScore = it[PropertyTable.sunnyScore],
                )
            }
    }

    override suspend fun getAllProperties(): List<Pair<Int, PropertyData>> = dbQuery {
        PropertyTable.selectAll().map {
            it[PropertyTable.id].value to PropertyData(
                postCode = it[PropertyTable.postCode],
                street = it[PropertyTable.street],
                city = it[PropertyTable.city],
                houseNumber = it[PropertyTable.houseNumber],
                customerId = it[PropertyTable.customerId].value,
                sunnyScore = it[PropertyTable.sunnyScore],
            )
        }
    }

    override suspend fun exists(propertyId: Int): Boolean = dbQuery {
        !PropertyTable.selectAll().where { PropertyTable.id eq propertyId }.empty()
    }
}
