package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

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
            it[metadata] = data.metadata
        }.value
    }

    override suspend fun exists(propertyId: Int): Boolean = dbQuery {
        PropertyTable.selectAll().where { PropertyTable.id eq propertyId }.any()
    }

    override suspend fun getPropertiesByCustomerId(customerId: Int): List<Pair<Int, PropertyData>> = dbQuery {
        PropertyTable.selectAll().where { PropertyTable.customerId eq customerId }
            .map { it[PropertyTable.id].value to it.toPropertyData() }
    }

    private fun ResultRow.toPropertyData() = PropertyData(
        postCode = this[PropertyTable.postCode],
        street = this[PropertyTable.street],
        city = this[PropertyTable.city],
        houseNumber = this[PropertyTable.houseNumber],
        customerId = this[PropertyTable.customerId].value,
        sunnyScore = this[PropertyTable.sunnyScore],
        metadata = this[PropertyTable.metadata]
    )
}