package com.qhack.application.infrastructure.openai

import com.qhack.application.services.openai.OpenAIService
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpenAIServiceTest {

    @Test
    fun `getSunnyScore should return parsed SunnyScoreResponse on success`() = runBlocking {
        val mockEngine = MockEngine { request ->
            val requestBody = request.body.toByteArray().decodeToString()
            assertTrue(requestBody.contains("\"model\":\"gpt-4o\""), "Request should contain model parameter")

            respond(
                content = """
                    {
                        "choices": [{
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "{\"address\": \"Main St 1, Berlin\", \"sunnyPlace\": 85, \"explanation\": \"Gute Lage mit viel Sonne.\"}"
                            },
                            "finish_reason": "stop"
                        }]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        val service = OpenAIService(httpClient)
        val result = service.getSunnyScore("Main St 1, Berlin")

        assertNotNull(result)
        assertEquals("Main St 1, Berlin", result.address)
        assertEquals(85, result.sunnyPlace)
    }

    @Test
    fun `generateOffer should return parsed OfferLLMResponse on success`(): Unit = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "choices": [{
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "{\"lead_summary\": {\"location\": {\"postal_code\": \"74238\", \"city\": \"Krautheim\", \"country\": \"DE\"}, \"primary_product\": \"heat_pump\", \"building_assumptions\": []}, \"market_context\": {\"summary\": \"test\", \"drivers\": [], \"why_now\": []}, \"subsidies\": [], \"recommended_offer\": {\"package_name\": \"test\", \"products\": [], \"reasoning\": [], \"estimated_cost_range_eur\": {\"min\": 0, \"max\": 0}, \"estimated_annual_savings_eur\": {\"min\": 0, \"max\": 0}, \"estimated_payback_years\": {\"min\": 0, \"max\": 0}}, \"alternative_offers\": [], \"financing_options\": [], \"sales_talking_points\": [], \"missing_information\": [], \"disclaimer\": \"test\"}"
                            },
                            "finish_reason": "stop"
                        }]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = OpenAIService(httpClient)
        val result = service.generateOffer(com.qhack.application.services.openai.OfferLLMRequest(
            postalCode = "74238",
            city = "Krautheim",
            country = "DE",
            primaryProduct = "heat_pump",
            constructionYear = 1985,
            heatingType = "gas"
        ))

        assertNotNull(result)
        assertEquals("74238", result.leadSummary.location.postalCode)
    }

    @Test
    fun `getSunnyScore should return null on 400 Bad Request`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"error": {"message": "Invalid request", "type": "invalid_request_error"}}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = OpenAIService(httpClient)
        val result = service.getSunnyScore("Main St 1, Berlin")

        assertNull(result)
    }

    @Test
    fun `getSunnyScore should return null on malformed response`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"unexpected_field": "some_value"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = OpenAIService(httpClient)
        val result = service.getSunnyScore("Main St 1, Berlin")

        assertNull(result)
    }

    @Test
    fun `getSunnyScore should return null on malformed inner JSON`() = runBlocking {
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "choices": [{
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "not a json"
                            },
                            "finish_reason": "stop"
                        }]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = OpenAIService(httpClient)
        val result = service.getSunnyScore("Main St 1, Berlin")

        assertNull(result)
    }
}
