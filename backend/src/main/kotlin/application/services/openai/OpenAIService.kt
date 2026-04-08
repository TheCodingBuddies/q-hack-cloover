package com.qhack.application.services.openai

import com.qhack.application.services.property.SunnyScoreResponse
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class OpenAIRequest(
    val model: String = "gpt-4o",
    val messages: List<OpenAIMessage>,
    val temperature: Double = 0.0
)

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

class OpenAIService(
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(OpenAIService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getSunnyScore(address: String): SunnyScoreResponse? {
        val dotenv = dotenv()
        val apiKey = dotenv["OPENAI_API_KEY"] ?: ""
        if (apiKey.isBlank()) {
            logger.warn("OpenAI API Key not configured, skipping Sunny Score request.")
            return null
        }

        val prompt = """
            Bewerte das Solarpotenzial (Sunny Score) für die folgende Adresse in Deutschland auf einer Skala von 0 bis 100.
            Berücksichtige die geografische Lage und die durchschnittliche Sonneneinstrahlung in dieser Region.
            Adresse: $address
            Deine Antwort muss zwingend ein valides JSON-Objekt sein. Antworte niemals mit Freitext vor oder nach dem JSON. Das JSON muss folgendem Schema entsprechen: • address: Die analysierte Adresse als String. • sunnyplace: Ein Integer von 0 (nie Sonne/vollständiger Schatten) bis 100 (immer Sonne/maximale Exposition). • explanation: Eine kurze Begründung (max. 1 Satz).
        """.trimIndent()

        return try {
            val httpResponse: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    OpenAIRequest(
                        messages = listOf(
                            OpenAIMessage(
                                role = "system",
                                content = "Du bist ein Experte für Solarenergie und Wetterdaten in Deutschland."
                            ),
                            OpenAIMessage(role = "user", content = prompt)
                        )
                    )
                )
            }

            if (!httpResponse.status.isSuccess()) {
                val errorBody = httpResponse.bodyAsText()
                logger.error("OpenAI API error: Status: ${httpResponse.status}, Body: $errorBody")
                return null
            }

            val response: OpenAIResponse = httpResponse.body()
            val content = response.choices.firstOrNull()?.message?.content?.trim()
                ?: return null

            json.decodeFromString<SunnyScoreResponse>(content)
        } catch (e: Exception) {
            logger.error("Error fetching Sunny Score: ${e.message}", e)
            null
        }
    }

    suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? {
        val dotenv = dotenv()
        val apiKey = dotenv["OPENAI_API_KEY"] ?: ""
        if (apiKey.isBlank()) {
            logger.warn("OpenAI API Key not configured, skipping Offer Generation request.")
            return null
        }

        val prompt = """
            Erstelle ein maßgeschneidertes Angebot für eine energetische Sanierung basierend auf folgenden Daten:
            Postleitzahl: ${request.postalCode}
            Stadt: ${request.city}
            Land: ${request.country}
            Gewünschtes Hauptprodukt: ${request.primaryProduct}
            Baujahr des Gebäudes: ${request.constructionYear}
            Aktueller Heizungstyp: ${request.heatingType}

            Deine Antwort muss zwingend ein valides JSON-Objekt sein, das dem bereitgestellten Schema entspricht.
            Antworte niemals mit Freitext vor oder nach dem JSON.
        """.trimIndent()

        return try {
            val httpResponse: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    OpenAIRequest(
                        messages = listOf(
                            OpenAIMessage(
                                role = "system",
                                content = "Du bist ein Experte für energetische Sanierung, Heizsysteme und staatliche Förderung in Deutschland."
                            ),
                            OpenAIMessage(role = "user", content = prompt)
                        )
                    )
                )
            }

            if (!httpResponse.status.isSuccess()) {
                val errorBody = httpResponse.bodyAsText()
                logger.error("OpenAI API error during offer generation: Status: ${httpResponse.status}, Body: $errorBody")
                return null
            }

            val response: OpenAIResponse = httpResponse.body()
            val content = response.choices.firstOrNull()?.message?.content?.trim()
                ?: return null

            json.decodeFromString<OfferLLMResponse>(content)
        } catch (e: Exception) {
            logger.error("Error generating offer: ${e.message}", e)
            null
        }
    }
}
