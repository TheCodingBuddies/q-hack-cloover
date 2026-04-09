package com.qhack.application.infrastructure.offer

interface OfferLLMCacheRepository {
    suspend fun getResponseByHash(hash: String): String?
    suspend fun save(hash: String, requestJson: String, responseJson: String): Int
}