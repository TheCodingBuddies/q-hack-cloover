package com.qhack.application.services.offer

import com.qhack.application.domain.offer.OfferData
import com.qhack.application.domain.offer.OfferStatus
import kotlinx.serialization.Serializable

@Serializable
data class OfferRequestDto(
    val customerId: Int,
    val propertyId: Int,
    val productId: Int
) {
    fun toDomain(): OfferData = OfferData(
        customerId = customerId,
        propertyId = propertyId,
        productId = productId,
        status = OfferStatus.PENDING
    )
}

@Serializable
data class OfferResponseDto(
    val id: Int,
    val customerId: Int,
    val propertyId: Int,
    val productId: Int,
    val status: String
) {
    companion object {
        fun fromDomain(id: Int, domain: OfferData): OfferResponseDto = OfferResponseDto(
            id = id,
            customerId = domain.customerId,
            propertyId = domain.propertyId,
            productId = domain.productId,
            status = domain.status.name
        )
    }
}
