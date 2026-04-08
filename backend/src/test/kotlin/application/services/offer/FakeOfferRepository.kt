package com.qhack.application.services.offer

import com.qhack.application.domain.offer.OfferData
import com.qhack.application.infrastructure.offer.OfferRepository

class FakeOfferRepository : OfferRepository {
    private var nextId = 1
    val offersList = mutableListOf<Pair<Int, OfferData>>()

    override suspend fun addOffer(data: OfferData): Int {
        val id = nextId++
        offersList.add(id to data)
        return id
    }

    override suspend fun getOfferById(id: Int): OfferData? {
        return offersList.find { it.first == id }?.second
    }

    override suspend fun getAllOffers(): List<Pair<Int, OfferData>> {
        return offersList
    }
}
