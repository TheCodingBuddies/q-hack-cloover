package com.qhack.application.services.customer

class CustomerService(private val customerRepository: CustomerRepository) {

    fun addCustomer(data: CustomerData): Int {
        return customerRepository.addCustomer(data)
    }
}