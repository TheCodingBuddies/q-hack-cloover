package com.qhack.application.infrastructure.customer

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

object Customers : IntIdTable("customers") {
    val firstName = varchar("first_name", 60)
    val lastName = varchar("last_name", 60)
    val birthDate = date("birth_date").nullable()
}