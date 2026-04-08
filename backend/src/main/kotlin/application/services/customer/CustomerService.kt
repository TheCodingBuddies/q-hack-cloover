package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.customer.CustomerRepository

class CustomerService(private val customerRepository: CustomerRepository) {

    suspend fun addCustomer(data: CustomerData): Int {
        return customerRepository.addCustomer(data)
    }

    suspend fun getAllCustomers(): List<Pair<Int, CustomerData>> {
        return customerRepository.getAllCustomers()
    }

    suspend fun getCustomersWithProperties(): Map<Int, Pair<CustomerData, List<Pair<Int, PropertyData>>>> {
        return customerRepository.getCustomersWithProperties()
    }

    suspend fun getCustomerWithProperties(customerId: Int): Pair<CustomerData, List<Pair<Int, PropertyData>>>? {
        return customerRepository.getCustomerWithProperties(customerId)
    }
}