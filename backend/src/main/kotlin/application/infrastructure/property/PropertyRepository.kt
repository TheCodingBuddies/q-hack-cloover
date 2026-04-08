package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData

interface PropertyRepository {
    suspend fun addProperty(data: PropertyData, score: Int, text: String): Int
    suspend fun getPropertiesByCustomerId(customerId: Int): List<PropertyData>
    suspend fun getAllProperties(): List<Pair<Int, PropertyData>>
    suspend fun exists(propertyId: Int): Boolean
}
