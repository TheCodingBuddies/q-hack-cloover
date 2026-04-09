package com.qhack.application.services.offer

import com.qhack.application.infrastructure.offer.OfferLLMCacheRepository

class FakeOfferLLMCacheRepository : OfferLLMCacheRepository {
    val store = mutableMapOf<String, String>()

    override suspend fun getResponseByHash(hash: String): String? = store[hash]

    override suspend fun save(hash: String, requestJson: String, responseJson: String): Int {
        store[hash] = responseJson
        return store.size
    }
}
