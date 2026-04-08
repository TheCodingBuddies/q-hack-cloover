package com.qhack.application.infrastructure.property

import com.qhack.application.domain.property.PropertyData

interface PropertyRepository {
    suspend fun addProperty(data: PropertyData): Int
}