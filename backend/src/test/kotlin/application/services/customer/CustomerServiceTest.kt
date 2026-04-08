package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.infrastructure.customer.CustomerRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals


class FakeCustomerRepository : CustomerRepository {
    private var nextId = 1
    val customers = mutableListOf<CustomerData>()

    override suspend fun addCustomer(data: CustomerData): Int {
        customers.add(data)
        return nextId++
    }

    override suspend fun exists(customerId: Int): Boolean {
        return customerId in 1..<nextId
    }
}

class CustomerServiceTest {

    @Test
    fun `addCustomer should call repository and return id`() = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)
        val customerData = CustomerData(
            firstName = "John",
            lastName = "Doe",
            birthDate = LocalDate.of(1990, 1, 1)
        )

        // When
        val result = service.addCustomer(customerData)

        // Then
        assertEquals(1, result)
        assertEquals(1, repository.customers.size)
        assertEquals(customerData, repository.customers[0])
    }

    @Test
    fun `addCustomer with null birthDate should call repository and return id`() = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)
        val customerData = CustomerData(
            firstName = "Jane",
            lastName = "Smith",
            birthDate = null
        )

        // When
        val result = service.addCustomer(customerData)

        // Then
        assertEquals(1, result)
        assertEquals(1, repository.customers.size)
        assertEquals(customerData, repository.customers[0])
    }
}
