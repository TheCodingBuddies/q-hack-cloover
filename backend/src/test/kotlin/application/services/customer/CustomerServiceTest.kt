package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.infrastructure.customer.CustomerRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals


class FakeCustomerRepository : CustomerRepository {
    private var nextId = 1
    val customersList = mutableListOf<Pair<Int, CustomerData>>()

    override suspend fun addCustomer(data: CustomerData): Int {
        val id = nextId++
        customersList.add(id to data)
        return id
    }

    override suspend fun exists(customerId: Int): Boolean {
        return customersList.any { it.first == customerId }
    }

    override suspend fun getAllCustomers(): List<Pair<Int, CustomerData>> {
        return customersList
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
        assertEquals(1, repository.customersList.size)
        assertEquals(customerData, repository.customersList[0].second)
    }

    @Test
    fun `getAllCustomers should return all customers from repository`() = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)
        val customer1 = CustomerData("John", "Doe", LocalDate.of(1990, 1, 1))
        val customer2 = CustomerData("Jane", "Smith", null)
        repository.addCustomer(customer1)
        repository.addCustomer(customer2)

        // When
        val result = service.getAllCustomers()

        // Then
        assertEquals(2, result.size)
        assertEquals(1, result[0].first)
        assertEquals(customer1, result[0].second)
        assertEquals(2, result[1].first)
        assertEquals(customer2, result[1].second)
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
        assertEquals(1, repository.customersList.size)
        assertEquals(customerData, repository.customersList[0].second)
    }
}
