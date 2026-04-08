package com.qhack.application.infrastructure.openai

import io.ktor.client.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenAIServiceTest {

    @Test
    fun `getSunnyScore should return parsed SunnyScoreResponse on success`() = runBlocking {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "id": "chatcmpl-123",
                        "object": "chat.completion",
                        "created": 1677652288,
                        "model": "gpt-4o-mini",
                        "choices": [{
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "{\"address\": \"Main St 1, Berlin\", \"sunnyplace\": 85, \"explanation\": \"Gute Lage mit viel Sonne.\"}"
                            },
                            "finish_reason": "stop"
                        }],
                        "usage": {
                            "prompt_tokens": 9,
                            "completion_tokens": 12,
                            "total_tokens": 21
                        }
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

        // When
        val result = service.getSunnyScore("Main St 1, Berlin")

        // Then
        assertNotNull(result)
        assertEquals("Main St 1, Berlin", result.address)
        assertEquals(85, result.sunnyPlace)
        assertEquals("Gute Lage mit viel Sonne.", result.explanation)
    }
}
