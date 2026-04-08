package com.qhack.application.services.customer

import com.qhack.application.infrastructure.Customers
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

data class CustomerData(
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate? = null,
)

class CustomerRepository {
    fun addCustomer(data: CustomerData): Int = transaction {
        Customers.insertAndGetId {
            it[firstName] = data.firstName
            it[lastName] = data.lastName
            it[birthDate] = data.birthDate
        }.value
    }
}
