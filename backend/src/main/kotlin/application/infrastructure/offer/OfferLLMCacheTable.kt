package com.qhack.application.infrastructure.offer

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object OfferLLMCacheTable : IntIdTable("offer_llm_cache") {
    val requestHash = varchar("request_hash", 128).uniqueIndex()
    val requestJson = text("request_json")
    val responseJson = text("response_json")
    val createdAt = timestamp("created_at").nullable()
}
