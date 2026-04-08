package com.qhack.application.domain.offer

data class OfferData(
    val customerId: Int,
    val propertyId: Int,
    val productId: Int,
    val status: OfferStatus = OfferStatus.PENDING
)

enum class OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
