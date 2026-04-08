package com.qhack.application.services.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.property.PropertyRepository
import io.ktor.server.plugins.NotFoundException

class PropertyService(
    private val propertyRepository: PropertyRepository,
    private val customerRepository: CustomerRepository
) {
    suspend fun addProperty(data: PropertyData, sunnyScore: Int, explanation: String): Int {
        if (!customerRepository.exists(data.customerId)) {
            throw NotFoundException("Customer with id ${data.customerId} does not exist")
        }
        return propertyRepository.addProperty(data, sunnyScore, explanation)
    }
}