package com.qhack.application.infrastructure.offer

import com.qhack.application.infrastructure.BaseRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class OfferLLMCacheRepositoryImpl : BaseRepository(), OfferLLMCacheRepository {
    override suspend fun getResponseByHash(hash: String): String? = dbQuery {
        OfferLLMCacheTable
            .selectAll()
            .where { OfferLLMCacheTable.requestHash eq hash }
            .map { it.toResponseJson() }
            .singleOrNull()
    }

    override suspend fun save(hash: String, requestJson: String, responseJson: String): Int = dbQuery {
        OfferLLMCacheTable.insertAndGetId {
            it[requestHash] = hash
            it[OfferLLMCacheTable.requestJson] = requestJson
            it[OfferLLMCacheTable.responseJson] = responseJson
        }.value
    }

    private fun ResultRow.toResponseJson(): String = this[OfferLLMCacheTable.responseJson]
}
