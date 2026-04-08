package com.qhack.application.infrastructure.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class CustomerRepositoryImpl : CustomerRepository, BaseRepository() {
    override suspend fun addCustomer(data: CustomerData): Int = dbQuery {
        Customers.insertAndGetId {
            it[firstName] = data.firstName
            it[lastName] = data.lastName
            it[birthDate] = data.birthDate
        }.value
    }

    override suspend fun exists(customerId: Int): Boolean = dbQuery {
        !Customers.selectAll().where { Customers.id eq customerId }.empty()
    }

    override suspend fun getAllCustomers(): List<Pair<Int, CustomerData>> = dbQuery {
        Customers.selectAll().map {
            it[Customers.id].value to CustomerData(
                firstName = it[Customers.firstName],
                lastName = it[Customers.lastName],
                birthDate = it[Customers.birthDate]
            )
        }
    }
}
