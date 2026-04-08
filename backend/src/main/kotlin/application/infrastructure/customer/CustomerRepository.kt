package com.qhack.application.infrastructure.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.property.PropertyData

interface CustomerRepository {
    suspend fun addCustomer(data: CustomerData): Int
    suspend fun exists(customerId: Int): Boolean
    suspend fun getAllCustomers(): List<Pair<Int, CustomerData>>
    suspend fun getCustomersWithProperties(): Map<Int, Pair<CustomerData, List<Pair<Int, PropertyData>>>>
    suspend fun getCustomerWithProperties(customerId: Int): Pair<CustomerData, List<Pair<Int, PropertyData>>>?
}
