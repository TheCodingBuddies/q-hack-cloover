package com.qhack.application.services.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.property.PropertyRepository
import com.qhack.application.services.customer.FakeCustomerRepository
import io.ktor.server.plugins.NotFoundException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FakePropertyRepository : PropertyRepository {
    private var nextId = 1
    val properties = mutableListOf<PropertyData>()

    override suspend fun addProperty(data: PropertyData): Int {
        properties.add(data)
        return nextId++
    }
}

class PropertyServiceTest {

    @Test
    fun `addProperty should call repository and return id if customer exists`(): Unit = runBlocking {
        // Given
        val customerRepo = FakeCustomerRepository()
        customerRepo.addCustomer(com.qhack.application.domain.customer.CustomerData("John", "Doe"))
        
        val propertyRepo = FakePropertyRepository()
        val service = PropertyService(propertyRepo, customerRepo)
        
        val propertyData = PropertyData(
            postCode = "12345",
            street = "Main St",
            city = "Berlin",
            houseNumber = "1A",
            customerId = 1
        )

        // When
        val result = service.addProperty(propertyData)

        // Then
        assertEquals(1, result)
        assertEquals(1, propertyRepo.properties.size)
        assertEquals(propertyData, propertyRepo.properties[0])
    }

    @Test
    fun `addProperty should throw exception if customer does not exist`(): Unit = runBlocking {
        // Given
        val customerRepo = FakeCustomerRepository()
        val propertyRepo = FakePropertyRepository()
        val service = PropertyService(propertyRepo, customerRepo)
        
        val propertyData = PropertyData(
            postCode = "12345",
            street = "Main St",
            city = "Berlin",
            houseNumber = "1A",
            customerId = 99
        )

        // When / Then
        assertFailsWith<NotFoundException> {
            service.addProperty(propertyData)
        }
    }
}
