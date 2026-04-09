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
    fun `generateOffer should return mock response`(): Unit = runBlocking {
        val httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val service = OpenAIServiceMock(httpClient)
        val request = OfferLLMRequest(
            propertyInfo = PropertyLLMInfo(
                postCode = "74238",
                street = "Street",
                houseNumber = "1",
                city = "Krautheim",
                country = "DE"
            ),
            customerProfile = CustomerLLMProfile(
                birthDate = "1985-05-15",
                metadata = mapOf("heating_type" to "gas")
            )
        )

        val response = service.generateOffer(request)

        assertNotNull(response)
        // Check for one of the mock postal codes
        val validPostalCodes = listOf("10115", "80331")
        assertTrue(validPostalCodes.contains(response.leadSummary.location.postalCode), "Postal code should be one of the mock values")
        
        assertNotNull(response.recommendedOffer)
        assertTrue(response.subsidies.isNotEmpty())
        assertTrue(response.financingOptions.isNotEmpty())
    }
}
