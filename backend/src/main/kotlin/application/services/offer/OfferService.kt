package com.qhack.application.services.offer

import com.qhack.application.domain.offer.OfferData
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.offer.OfferRepository
import com.qhack.application.infrastructure.product.ProductRepository
import com.qhack.application.infrastructure.property.PropertyRepository
import io.ktor.server.plugins.*


class OfferService(
    private val offerRepository: OfferRepository,
    private val customerRepository: CustomerRepository,
    private val propertyRepository: PropertyRepository,
    private val productRepository: ProductRepository
) {

    suspend fun createOffer(data: OfferData): Int {
        if (!customerRepository.exists(data.customerId)) {
            throw NotFoundException("Customer with id ${data.customerId} does not exist")
        }
        if (!propertyRepository.exists(data.propertyId)) {
            throw NotFoundException("Property with id ${data.propertyId} does not exist")
        }
        if (!productRepository.exists(data.productId)) {
            throw NotFoundException("Product with id ${data.productId} does not exist")
        }

        return offerRepository.addOffer(data)
    }

    suspend fun getAllOffers(): List<Pair<Int, OfferData>> {
        return offerRepository.getAllOffers()
    }

    suspend fun getOfferById(id: Int): OfferData? {
        return offerRepository.getOfferById(id)
    }
}
