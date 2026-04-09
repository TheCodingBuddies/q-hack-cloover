package com.qhack.application.services.offer

import com.qhack.application.services.openai.IOpenAIService
import com.qhack.application.services.openai.OfferLLMRequest
import com.qhack.application.services.openai.OfferLLMResponse
import com.qhack.application.services.property.SunnyScoreResponse

class FakeOpenAIService : IOpenAIService {
    override suspend fun getSunnyScore(address: String): SunnyScoreResponse? = null

    override suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? = null
}
