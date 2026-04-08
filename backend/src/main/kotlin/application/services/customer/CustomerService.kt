package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.property.PropertyRepository

class CustomerService(
    private val customerRepository: CustomerRepository,
    private val propertyRepository: PropertyRepository,
) {

    suspend fun addCustomer(data: CustomerData): Int {
        return customerRepository.addCustomer(data)
    }

    suspend fun getAllCustomers(): List<Pair<Int, CustomerData>> {
        return customerRepository.getAllCustomers()
    }

    suspend fun getCustomerById(id: Int): Pair<CustomerData, PropertyData?>? {
        val customer = customerRepository.getAllCustomers()
            .firstOrNull { (customerId, _) -> customerId == id }
            ?: return null
        val (_, customerData) = customer
        val property = propertyRepository.getPropertiesByCustomerId(id).firstOrNull()
        return customerData to property
    }

    suspend fun getAllCustomersWithProperties(): List<Triple<Int, CustomerData, PropertyData?>> {
        return customerRepository.getAllCustomers().map { (id, customerData) ->
            val property = propertyRepository.getPropertiesByCustomerId(id).firstOrNull()
            Triple(id, customerData, property)
        }
    }
}
