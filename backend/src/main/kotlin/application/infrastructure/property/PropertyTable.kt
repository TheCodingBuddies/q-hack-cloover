package com.qhack.application.infrastructure.property

import com.qhack.application.infrastructure.customer.Customers
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PropertyTable : IntIdTable("properties") {
    val postCode = varchar("postcode", 10)
    val street = varchar("street", 100)
    val city = varchar("city", 100)
    val houseNumber = varchar("house_number", 10)
    val customerId = reference("customer_id", Customers, onDelete = ReferenceOption.CASCADE)
    val sunnyScore = integer("sunny_score").nullable()
}