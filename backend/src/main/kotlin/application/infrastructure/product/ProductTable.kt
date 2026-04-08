package com.qhack.application.infrastructure.product

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * Exposed-Definition der "products"-Tabelle.
 */
object ProductTable : IntIdTable("products") {
    val name = varchar("name", 100)
}
