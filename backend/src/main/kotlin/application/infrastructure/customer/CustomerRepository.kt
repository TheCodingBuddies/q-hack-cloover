package com.qhack.application.infrastructure.customer

import com.qhack.application.domain.customer.CustomerData


interface CustomerRepository {
    suspend fun addCustomer(data: CustomerData): Int
    suspend fun exists(customerId: Int): Boolean
}
