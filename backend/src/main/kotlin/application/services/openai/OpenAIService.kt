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
) : IOpenAIService {
    private val logger = LoggerFactory.getLogger(OpenAIService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getSunnyScore(address: String): SunnyScoreResponse? {
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
            WICHTIG: Verwende im JSON exakt den Feldnamen "sunnyplace" für den Score.
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

            val cleanJson = stripMarkdown(content)
            json.decodeFromString<SunnyScoreResponse>(cleanJson)
        } catch (e: Exception) {
            logger.error("Error fetching Sunny Score: ${e.message}", e)
            null
        }
    }

    private fun stripMarkdown(content: String): String {
        return content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    override suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? {
        val dotenv = dotenv()
        val apiKey = dotenv["OPENAI_API_KEY"] ?: ""
        if (apiKey.isBlank()) {
            logger.warn("OpenAI API Key not configured, skipping Offer Generation request.")
            return null
        }

        val requestJson = json.encodeToString(OfferLLMRequest.serializer(), request)

        val systemPrompt = """
            Du bist ein hochqualifizierter Energieberater für den deutschen Markt. Deine Aufgabe ist es, basierend auf Gebäudedaten, dem Kundenprofil und optionalen Metadaten ein detailliertes Angebot für energetische Sanierungsmaßnahmen zu erstellen.

            **Besondere Berücksichtigung der Standortfaktoren:**
            Nutze die bereitgestellte Adresse (Straße, Hausnummer, PLZ, Stadt), um die regionalen Gegebenheiten zu bewerten. **Berücksichtige insbesondere die durchschnittliche jährliche Sonnenstundenzeit an diesem spezifischen Standort**, um die Wirtschaftlichkeit und Dimensionierung von Photovoltaik-Anlagen realistisch einzuschätzen.

            Berücksichtige zusätzlich:
            1. **Regionale Gegebenheiten:** Lokale klimatische Bedingungen und regionale Besonderheiten.
            2. **Gesetzeslage:** Aktuelle deutsche Gesetze (z. B. GEG, 65%-EE-Vorgabe, kommunale Wärmeplanung).
            3. **Fördermittel:** Identifiziere relevante Förderungen (KfW, BAFA) basierend auf dem Gebäude- und Heizungstyp.
            4. **Wirtschaftlichkeit:** Schätze Kosten, Einsparungen und Amortisation fundiert ein.

            **WICHTIG:** Antworte **ausschließlich** im JSON-Format. Die Struktur muss exakt dem vorgegebenen Schema entsprechen.
        """.trimIndent()

        val userPrompt = """
            Erstelle ein Energieberatungs-Angebot für folgendes Objekt und Kundenprofil. Analysiere dabei die Sonnenstundenzeit für die angegebene Adresse, um die Empfehlungen zu präzisieren.

            ### EINGABEDATEN (JSON):
            $requestJson

            ### ANFORDERUNG:
            Generiere eine Antwort, die exakt die folgende JSON-Struktur erfüllt. Fülle alle Felder mit fachlich fundierten Inhalten aus. Wenn Daten fehlen, triff plausible Annahmen basierend auf dem Baujahr und dem Standort und liste diese unter `missing_information` auf.

            ### ZIEL-STRUKTUR (JSON):
            {
              "lead_summary": {
                "location": { "postal_code": "", "street": "", "house_number": "", "city": "", "country": "" },
                "primary_products": [""]
              },
              "market_context": {
                "summary": "Analysiere hier die Situation basierend auf GEG, regionalen Faktoren und der solaren Strahlungsintensität am Standort.",
                "drivers": ["Treiber 1", "Treiber 2"],
                "why_now": ["Grund 1", "Grund 2"]
              },
              "subsidies": [
                { "name": "", "status": "", "relevance": "", "estimated_effect_eur": 0.0, "notes": "" }
              ],
              "recommended_offer": {
                "package_name": "",
                "products": [""],
                "reasoning": [""],
                "estimated_cost_range_eur": { "min": 0, "max": 0 },
                "estimated_annual_savings_eur": { "min": 0, "max": 0 },
                "estimated_payback_years": { "min": 0.0, "max": 0.0 }
              },
              "alternative_offers": [
                { "package_name": "", "products": [""], "positioning": "", "estimated_cost_range_eur": { "min": 0, "max": 0 } }
              ],
              "financing_options": [
                { "scenario": "Barzahlung", "down_payment_eur": 0, "loan_amount_eur": 0, "term_months": 0, "apr_percent": 0.0, "monthly_payment_eur": 0, "notes": "" }
              ],
              "sales_talking_points": ["Argument 1", "Argument 2"],
              "missing_information": ["Info 1", "Info 2"],
              "disclaimer": "Rechtlicher Hinweis zur Unverbindlichkeit."
            }
        """.trimIndent()

        return try {
            val httpResponse: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    OpenAIRequest(
                        messages = listOf(
                            OpenAIMessage(role = "system", content = systemPrompt),
                            OpenAIMessage(role = "user", content = userPrompt)
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

            val cleanJson = stripMarkdown(content)
            json.decodeFromString<OfferLLMResponse>(cleanJson)
        } catch (e: Exception) {
            logger.error("Error generating offer: ${e.message}", e)
            null
        }
    }
}
