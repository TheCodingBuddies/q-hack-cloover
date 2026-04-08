package com.qhack.application.infrastructure.offer

import com.qhack.application.domain.offer.OfferData
import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class OfferRepositoryImpl : BaseRepository(), OfferRepository {
    override suspend fun addOffer(data: OfferData): Int = dbQuery {
        OfferTable.insertAndGetId {
            it[customerId] = data.customerId
            it[propertyId] = data.propertyId
            it[productId] = data.productId
            it[status] = data.status
        }.value
    }

    override suspend fun getOfferById(id: Int): OfferData? = dbQuery {
        OfferTable.selectAll().where { OfferTable.id eq id }
            .map { it.toOfferData() }
            .singleOrNull()
    }

    override suspend fun getAllOffers(): List<Pair<Int, OfferData>> = dbQuery {
        OfferTable.selectAll().map { it[OfferTable.id].value to it.toOfferData() }
    }

    private fun ResultRow.toOfferData() = OfferData(
        customerId = this[OfferTable.customerId].value,
        propertyId = this[OfferTable.propertyId].value,
        productId = this[OfferTable.productId].value,
        status = this[OfferTable.status]
    )
}
