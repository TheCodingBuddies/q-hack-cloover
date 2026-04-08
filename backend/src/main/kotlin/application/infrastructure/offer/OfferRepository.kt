package com.qhack.application.infrastructure.offer

import com.qhack.application.domain.offer.OfferData

interface OfferRepository {
    suspend fun addOffer(data: OfferData): Int
    suspend fun getOfferById(id: Int): OfferData?
    suspend fun getAllOffers(): List<Pair<Int, OfferData>>
}
