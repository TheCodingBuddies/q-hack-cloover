package com.qhack.application.services.openai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenAiServiceMockTest {

    @Test
    fun `generateOffer should return mock response with requested data`(): Unit = runBlocking {
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = OpenAIServiceMock(httpClient)
        val request = OfferLLMRequest(
            postalCode = "74238",
            city = "Krautheim",
            country = "DE",
            primaryProduct = "heat_pump",
            constructionYear = 1985,
            heatingType = "gas"
        )

        val response = service.generateOffer(request)

        assertNotNull(response)
        assertEquals("74238", response.leadSummary.location.postalCode)
        assertEquals("Krautheim", response.leadSummary.location.city)
        assertTrue(response.leadSummary.primaryProducts.contains("heat_pump"))
        assertNotNull(response.recommendedOffer)
        assertTrue(response.subsidies.isNotEmpty())
        assertTrue(response.financingOptions.isNotEmpty())
    }
}
