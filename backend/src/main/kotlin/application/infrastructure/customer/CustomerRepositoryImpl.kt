package com.qhack.application.infrastructure.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.insertAndGetId

class CustomerRepositoryImpl : CustomerRepository, BaseRepository() {
    override suspend fun addCustomer(data: CustomerData): Int = dbQuery {
        Customers.insertAndGetId {
            it[firstName] = data.firstName
            it[lastName] = data.lastName
            it[birthDate] = data.birthDate
        }.value
    }
}