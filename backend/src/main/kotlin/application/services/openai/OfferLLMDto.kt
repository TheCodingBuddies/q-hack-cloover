package com.qhack.application.services.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OfferLLMRequest(
    @SerialName("postal_code") val postalCode: String,
    val city: String,
    val country: String,
    @SerialName("primary_product") val primaryProduct: String,
    @SerialName("construction_year") val constructionYear: Int,
    @SerialName("heating_type") val heatingType: String
)

@Serializable
data class OfferLLMResponse(
    @SerialName("lead_summary") val leadSummary: LeadSummary,
    @SerialName("market_context") val marketContext: MarketContext,
    val subsidies: List<Subsidy>,
    @SerialName("recommended_offer") val recommendedOffer: RecommendedOffer,
    @SerialName("alternative_offers") val alternativeOffers: List<AlternativeOffer>,
    @SerialName("financing_options") val financingOptions: List<FinancingOption>,
    @SerialName("sales_talking_points") val salesTalkingPoints: List<String>,
    @SerialName("missing_information") val missingInformation: List<String>,
    val disclaimer: String
)

@Serializable
data class LeadSummary(
    val location: Location,
    @SerialName("primary_products") val primaryProducts: List<String>
)

@Serializable
data class Location(
    @SerialName("postal_code") val postalCode: String,
    val street: String? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    val city: String,
    val country: String
)

@Serializable
data class MarketContext(
    val summary: String,
    val drivers: List<String>,
    @SerialName("why_now") val whyNow: List<String>
)

@Serializable
data class Subsidy(
    val name: String,
    val status: String,
    val relevance: String,
    @SerialName("estimated_effect_eur") val estimatedEffectEur: Double,
    val notes: String
)

@Serializable
data class RecommendedOffer(
    @SerialName("package_name") val packageName: String,
    val products: List<String>,
    val reasoning: List<String>,
    @SerialName("estimated_cost_range_eur") val estimatedCostRangeEur: RangeEur,
    @SerialName("estimated_annual_savings_eur") val estimatedAnnualSavingsEur: RangeEur,
    @SerialName("estimated_payback_years") val estimatedPaybackYears: RangeDouble
)

@Serializable
data class RangeDouble(
    val min: Double,
    val max: Double
)

@Serializable
data class AlternativeOffer(
    @SerialName("package_name") val packageName: String,
    val products: List<String>,
    val positioning: String,
    @SerialName("estimated_cost_range_eur") val estimatedCostRangeEur: RangeEur
)

@Serializable
data class FinancingOption(
    val scenario: String,
    @SerialName("down_payment_eur") val downPaymentEur: Int,
    @SerialName("loan_amount_eur") val loanAmountEur: Int,
    @SerialName("term_months") val termMonths: Int,
    @SerialName("apr_percent") val aprPercent: Double,
    @SerialName("monthly_payment_eur") val monthlyPaymentEur: Int,
    val notes: String
)

@Serializable
data class RangeEur(
    val min: Int,
    val max: Int
)
