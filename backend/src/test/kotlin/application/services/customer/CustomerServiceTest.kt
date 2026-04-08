package com.qhack.application.services.customer

import com.qhack.application.infrastructure.customer.CustomerData
import com.qhack.application.infrastructure.customer.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomerServiceTest {
    private val repository = mockk<CustomerRepository>()
    private val service = CustomerService(repository)

    @Test
    fun `addCustomer should call repository and return id`() {
        // Given
        val customerData = CustomerData(
            firstName = "John",
            lastName = "Doe",
            birthDate = LocalDate.of(1990, 1, 1)
        )
        val expectedId = 42
        every { repository.addCustomer(customerData) } returns expectedId

        // When
        val result = service.addCustomer(customerData)

        // Then
        assertEquals(expectedId, result)
        verify(exactly = 1) { repository.addCustomer(customerData) }
    }
}
