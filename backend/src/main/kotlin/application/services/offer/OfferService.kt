package com.qhack.application.services.offer

import com.qhack.application.domain.offer.OfferData
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.offer.OfferLLMCacheRepository
import com.qhack.application.infrastructure.offer.OfferRepository
import com.qhack.application.infrastructure.product.ProductRepository
import com.qhack.application.infrastructure.property.PropertyRepository
import com.qhack.application.services.openai.IOpenAIService
import com.qhack.application.services.openai.OfferLLMRequest
import com.qhack.application.services.openai.OfferLLMResponse
import io.ktor.server.plugins.*
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class OfferService(
    private val offerRepository: OfferRepository,
    private val customerRepository: CustomerRepository,
    private val propertyRepository: PropertyRepository,
    private val productRepository: ProductRepository,
    private val openAIService: IOpenAIService,
    private val offerLLMCacheRepository: OfferLLMCacheRepository,
    private val json: Json = Json { ignoreUnknownKeys = true }
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

    suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? {
        val requestJson = json.encodeToString(OfferLLMRequest.serializer(), request)
        val hash = sha256(requestJson)

        // Cache-Hit?
        offerLLMCacheRepository.getResponseByHash(hash)?.let { cachedJson ->
            return json.decodeFromString(OfferLLMResponse.serializer(), cachedJson)
        }

        // Cache-Miss -> LLM aufrufen
        val response = openAIService.generateOffer(request) ?: return null
        val responseJson = json.encodeToString(OfferLLMResponse.serializer(), response)
        offerLLMCacheRepository.save(hash, requestJson, responseJson)
        return response
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { b -> "%02x".format(b) }
    }
}
