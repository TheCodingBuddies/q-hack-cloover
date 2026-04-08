package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData

interface PropertyRepository {
    suspend fun addProperty(data: PropertyData, score: Int, text: String): Int
    suspend fun exists(propertyId: Int): Boolean
}