package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.customer.CustomerRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    override suspend fun getCustomersWithProperties(): Map<Int, Pair<CustomerData, List<Pair<Int, PropertyData>>>> {
        return customersList.associate { (customerId, customerData) ->
            customerId to (customerData to emptyList<Pair<Int, PropertyData>>())
        }
    }

    override suspend fun getCustomerWithProperties(customerId: Int): Pair<CustomerData, List<Pair<Int, PropertyData>>>? {
        val customer = customersList.find { it.first == customerId }?.second ?: return null
        return customer to emptyList()
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
    fun `getCustomersWithProperties should return customers from repository`() = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)
        val customer1 = CustomerData("John", "Doe", LocalDate.of(1990, 1, 1))
        repository.addCustomer(customer1)

        // When
        val result = service.getCustomersWithProperties()

        // Then
        assertEquals(1, result.size)
        assertEquals(customer1, result[1]?.first)
    }

    @Test
    fun `getCustomerWithProperties should return customer from repository`(): Unit = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)
        val customer1 = CustomerData("John", "Doe", LocalDate.of(1990, 1, 1))
        val id = repository.addCustomer(customer1)

        // When
        val result = service.getCustomerWithProperties(id)

        // Then
        assertNotNull(result)
        assertEquals(customer1, result.first)
    }

    @Test
    fun `getCustomerWithProperties should return null if customer does not exist`(): Unit = runBlocking {
        // Given
        val repository = FakeCustomerRepository()
        val service = CustomerService(repository)

        // When
        val result = service.getCustomerWithProperties(999)

        // Then
        assertNull(result)
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
