package com.qhack.application.infrastructure.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.infrastructure.BaseRepository
import com.qhack.application.infrastructure.property.PropertyTable
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

    override suspend fun getCustomersWithProperties(): Map<Int, Pair<CustomerData, List<Pair<Int, PropertyData>>>> =
        dbQuery {
            val customers = Customers.selectAll().map {
                val customerId = it[Customers.id].value
                val data = CustomerData(
                    firstName = it[Customers.firstName],
                    lastName = it[Customers.lastName],
                    birthDate = it[Customers.birthDate]
                )
                customerId to data
            }.toMap()

            val properties = PropertyTable.selectAll().map {
                val id = it[PropertyTable.id].value
                val customerId = it[PropertyTable.customerId].value
                val data = PropertyData(
                    postCode = it[PropertyTable.postCode],
                    street = it[PropertyTable.street],
                    city = it[PropertyTable.city],
                    houseNumber = it[PropertyTable.houseNumber],
                    customerId = customerId,
                    sunnyScore = it[PropertyTable.sunnyScore],
                    metadata = it[PropertyTable.metadata]
                )
                customerId to (id to data)
            }.groupBy({ it.first }, { it.second })

            customers.mapValues { (id, data) ->
                data to (properties[id] ?: emptyList())
            }
        }

    override suspend fun getCustomerWithProperties(customerId: Int): Pair<CustomerData, List<Pair<Int, PropertyData>>>? =
        dbQuery {
            val customer = Customers.selectAll().where { Customers.id eq customerId }.map {
                CustomerData(
                    firstName = it[Customers.firstName],
                    lastName = it[Customers.lastName],
                    birthDate = it[Customers.birthDate]
                )
            }.singleOrNull() ?: return@dbQuery null

            val properties = PropertyTable.selectAll().where { PropertyTable.customerId eq customerId }.map {
                val id = it[PropertyTable.id].value
                val data = PropertyData(
                    postCode = it[PropertyTable.postCode],
                    street = it[PropertyTable.street],
                    city = it[PropertyTable.city],
                    houseNumber = it[PropertyTable.houseNumber],
                    customerId = customerId,
                    sunnyScore = it[PropertyTable.sunnyScore],
                    metadata = it[PropertyTable.metadata]
                )
                id to data
            }

            customer to properties
        }
}