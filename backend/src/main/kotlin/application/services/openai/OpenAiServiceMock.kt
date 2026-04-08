package com.qhack.application.services.openai

import com.qhack.application.services.property.SunnyScoreResponse
import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class OpenAIServiceMock (
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(OpenAIService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getSunnyScore(address: String): SunnyScoreResponse? {
        if (address.contains("Berlin"))
            return SunnyScoreResponse(
                address,
                50,
                "Zentrale Innenstadtlage mit dichter Bebauung führt zu wechselnden Verschattungen trotz grundsätzlich guter Sonneneinstrahlung."
            )
        return SunnyScoreResponse(
            address,
            85,
            "Küstenlage mit häufigem Nebel reduziert direkte Sonneneinstrahlung trotz offener Bebauung in Teilen."
        )
    }
}
