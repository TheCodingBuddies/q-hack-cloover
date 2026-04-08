package com.qhack.application.services.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.infrastructure.customer.CustomerRepository

class CustomerService(private val customerRepository: CustomerRepository) {

    suspend fun addCustomer(data: CustomerData): Int {
        return customerRepository.addCustomer(data)
    }
}