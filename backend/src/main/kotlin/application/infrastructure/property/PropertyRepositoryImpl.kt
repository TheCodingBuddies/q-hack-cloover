package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.insertAndGetId

class PropertyRepositoryImpl : PropertyRepository, BaseRepository() {
    override suspend fun addProperty(data: PropertyData): Int = dbQuery {
        PropertyTable.insertAndGetId {
            it[postCode] = data.postCode
            it[street] = data.street
            it[city] = data.city
            it[houseNumber] = data.houseNumber
            it[customerId] = data.customerId
        }.value
    }
}