package com.qhack.application.infrastructure.offer

import com.qhack.application.domain.offer.OfferStatus
import com.qhack.application.infrastructure.customer.Customers
import com.qhack.application.infrastructure.product.ProductTable
import com.qhack.application.infrastructure.property.PropertyTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption


object OfferTable : IntIdTable("offers") {
    val customerId = reference("customer_id", Customers, onDelete = ReferenceOption.CASCADE)
    val propertyId = reference("property_id", PropertyTable, onDelete = ReferenceOption.CASCADE)
    val productId = reference("product_id", ProductTable, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 20, OfferStatus::class).default(OfferStatus.PENDING)
}
