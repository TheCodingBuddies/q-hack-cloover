package com.qhack.application.services.openai

import com.qhack.application.services.property.SunnyScoreResponse

interface IOpenAIService {
    suspend fun getSunnyScore(address: String): SunnyScoreResponse?
    suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse?
}
