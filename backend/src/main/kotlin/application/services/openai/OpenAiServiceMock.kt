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

    suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? {
        val mockJson = """
        {
            "lead_summary": {
                "location": {
                    "postal_code": "${request.postalCode}",
                    "street": "Musterstraße",
                    "house_number": "123",
                    "city": "${request.city}",
                    "country": "${request.country}"
                },
                "primary_products": ["${request.primaryProduct}"]
            },
            "market_context": {
                "summary": "Für einen Altbau mit ${request.heatingType}heizung ist der Umstieg auf eine klimafreundliche Heizung wirtschaftlich und regulatorisch relevant, weil fossile Wärmekosten langfristig unter Druck bleiben, die 65%-EE-Vorgaben den Heizungsmarkt prägen und weiterhin attraktive Förderkulissen verfügbar sind.",
                "drivers": [
                    "Gasheizungen bleiben anfällig für Preis- und CO2-Kostenrisiken.",
                    "Das GEG setzt für neue Heizungen grundsätzlich den 65%-Erneuerbare-Energien-Rahmen.",
                    "Die kommunale Wärmeplanung erhöht mittelfristig den Handlungsdruck und die Entscheidungssicherheit.",
                    "Die Heizungsförderung kann die Anfangsinvestition deutlich reduzieren."
                ],
                "why_now": [
                    "Fördermittel senken aktuell die Einstiegskosten spürbar.",
                    "Ein Heizungstausch vor einem Defekt ist planbarer und oft wirtschaftlicher als eine Havarieentscheidung.",
                    "Bei älteren Gasheizungen steigt typischerweise das Risiko für Reparaturen und Effizienzverluste.",
                    "Frühes Handeln schafft mehr Auswahl bei Technik, Installationspartnern und Finanzierung."
                ]
            },
            "subsidies": [
                {
                    "name": "KfW Heizungsförderung für Privatpersonen – Wohngebäude (458)",
                    "status": "potenziell relevant",
                    "relevance": "Sehr hoch bei selbstgenutztem Bestandswohngebäude und Tausch einer fossilen Heizung gegen eine klimafreundliche Heizung.",
                    "estimated_effect_eur": 9000.0,
                    "notes": "Die tatsächliche Förderhöhe hängt von Förderfähigkeit, förderfähigen Kosten und persönlichen Zuschlagsvoraussetzungen ab; in der Praxis ist eine Wärmepumpe häufig der zentrale Förderhebel."
                },
                {
                    "name": "KfW Ergänzungskredit Wohngebäude (358/359)",
                    "status": "potenziell relevant",
                    "relevance": "Hoch, wenn Zuschuss bewilligt wurde und der Kunde Restkosten finanzieren möchte.",
                    "estimated_effect_eur": 0.0,
                    "notes": "Kein direkter Zuschuss, aber potenziell zinsgünstige Finanzierung; bei bestimmten Einkommensgrenzen ist ein zusätzlicher Zinsvorteil möglich."
                }
            ],
            "recommended_offer": {
                "package_name": "Empfohlen: Luft-Wasser-Wärmepumpe mit Heizungsoptimierung",
                "products": [
                    "Luft-Wasser-Wärmepumpe im Bereich ca. 8-12 kW",
                    "Hydraulischer Abgleich",
                    "Pufferspeicher / Warmwasserspeicher je nach Auslegung",
                    "Anpassung einzelner Heizflächen falls erforderlich",
                    "Einbindung in bestehendes Wärmeverteilsystem",
                    "Optional: Smart-Home- / Energiemanagement-Basis"
                ],
                "reasoning": [
                    "Für einen typischen Wohngebäude-Bestand aus den 1980er-Jahren ist die Luft-Wasser-Wärmepumpe oft die wirtschaftlich sinnvollste Einstiegslösung, sofern die Systemtemperaturen beherrschbar sind.",
                    "Der Wechsel von ${request.heatingType} auf Wärmepumpe adressiert das zentrale Kosten- und Regulierungsrisiko des Bestands.",
                    "Ohne weitere Gebäudedaten ist eine konservative Empfehlung mit Fokus auf Heizungsoptimierung sinnvoller als ein technisch maximaler Ausbau.",
                    "Die Förderlandschaft verbessert die Wirtschaftlichkeit deutlich und macht das Angebot vertrieblich gut argumentierbar."
                ],
                "estimated_cost_range_eur": {
                    "min": 22000,
                    "max": 34000
                },
                "estimated_annual_savings_eur": {
                    "min": 700,
                    "max": 1800
                },
                "estimated_payback_years": {
                    "min": 8.0,
                    "max": 15.0
                }
            },
            "alternative_offers": [
                {
                    "package_name": "Einsteiger: Wärmepumpe Basic",
                    "products": [
                        "Luft-Wasser-Wärmepumpe",
                        "Standardinstallation",
                        "Grundlegende Systemeinbindung"
                    ],
                    "positioning": "Für Kunden mit Fokus auf schnellen Austausch, solide Förderung und geringere Anfangsinvestition.",
                    "estimated_cost_range_eur": {
                        "min": 18000,
                        "max": 27000
                    }
                },
                {
                    "package_name": "Komfort: Wärmepumpe + Heizflächen-/Effizienzupgrade",
                    "products": [
                        "Luft-Wasser-Wärmepumpe",
                        "Hydraulischer Abgleich",
                        "Teilweiser Heizkörpertausch",
                        "Optimierung von Regelung und Speichertechnik"
                    ],
                    "positioning": "Für Kunden, bei denen Vorlauftemperatur, Komfort oder Effizienz im Altbau gezielt verbessert werden sollen.",
                    "estimated_cost_range_eur": {
                        "min": 26000,
                        "max": 38000
                    }
                },
                {
                    "package_name": "Zukunftspaket: Wärmepumpe + PV-Vorbereitung",
                    "products": [
                        "Luft-Wasser-Wärmepumpe",
                        "Energiemanagement-Vorbereitung",
                        "Schnittstellen für spätere PV- oder Speicherintegration"
                    ],
                    "positioning": "Für Kunden, die heute die Heizung modernisieren und in einem zweiten Schritt Solar nachziehen wollen.",
                    "estimated_cost_range_eur": {
                        "min": 23000,
                        "max": 36000
                    }
                }
            ],
            "financing_options": [
                {
                    "scenario": "Barzahlung",
                    "down_payment_eur": 22000,
                    "loan_amount_eur": 0,
                    "term_months": 0,
                    "apr_percent": 0.0,
                    "monthly_payment_eur": 0,
                    "notes": "Maximale Einfachheit; Förderung reduziert die effektive Nettobelastung im Nachgang bzw. gemäß Förderprozess."
                },
                {
                    "scenario": "Teilfinanzierung",
                    "down_payment_eur": 7000,
                    "loan_amount_eur": 16000,
                    "term_months": 120,
                    "apr_percent": 4.9,
                    "monthly_payment_eur": 169,
                    "notes": "Geeignet für Kunden, die Eigenkapital einsetzen, aber Liquidität schonen möchten."
                },
                {
                    "scenario": "Vollfinanzierung",
                    "down_payment_eur": 0,
                    "loan_amount_eur": 24000,
                    "term_months": 180,
                    "apr_percent": 5.2,
                    "monthly_payment_eur": 192,
                    "notes": "Für Kunden mit geringem Eigenkapital; Kombination mit Fördermitteln und ggf. Ergänzungskredit prüfen."
                }
            ],
            "sales_talking_points": [
                "Sie tauschen ein fossiles Preisrisiko gegen ein zukunftsfähiges Heizsystem mit Förderung.",
                "Gerade bei einem Gebäude mit Baujahr ${request.constructionYear} ist eine Wärmepumpe oft wirtschaftlich machbar, wenn die Systemauslegung sauber geplant wird.",
                "Heute geplant ist günstiger und stressfreier als später unter Defektdruck.",
                "Wir empfehlen nicht pauschal das teuerste Paket, sondern die Variante mit dem besten Verhältnis aus Investition, Förderung und Betriebskosten."
            ],
            "missing_information": [
                "Gebäudetyp und Wohnfläche",
                "Dämmstandard / Sanierungsstand",
                "Jährlicher Gasverbrauch in kWh",
                "Aktuelles Heizsystemalter",
                "Vorlauftemperaturen im Winter",
                "Art und Größe der Heizkörper / Flächenheizung",
                "Anzahl der Bewohner",
                "Ob Selbstnutzung oder Vermietung vorliegt",
                "Verfügbares Eigenkapital und gewünschte Monatsrate"
            ],
            "disclaimer": "Dieses Angebot ist fiktiv und dient der Vertriebs-Vorqualifizierung. Förderfähigkeit, technische Machbarkeit, exakte Dimensionierung, reale Einsparungen und Finanzierungskonditionen müssen vor Vertragsabschluss durch Vor-Ort-Termin, Verbrauchsdaten, Heizlastprüfung und finale Förderprüfung validiert werden."
        }
        """.trimIndent()
        return json.decodeFromString<OfferLLMResponse>(mockJson)
    }
}
