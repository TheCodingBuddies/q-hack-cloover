package com.qhack.application.services.offer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.product.ProductData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.services.customer.FakeCustomerRepository
import com.qhack.application.services.product.FakeProductRepository
import com.qhack.application.services.property.FakePropertyRepository
import com.qhack.application.services.openai.OfferLLMRequest
import com.qhack.application.services.openai.PropertyLLMInfo
import com.qhack.application.services.openai.CustomerLLMProfile
import com.qhack.application.services.openai.OfferLLMResponse
import com.qhack.application.services.openai.LeadSummary
import com.qhack.application.services.openai.Location
import com.qhack.application.services.openai.MarketContext
import com.qhack.application.services.openai.Subsidy
import com.qhack.application.services.openai.RecommendedOffer
import com.qhack.application.services.openai.RangeEur
import com.qhack.application.services.openai.RangeDouble
import com.qhack.application.services.openai.AlternativeOffer
import com.qhack.application.services.openai.FinancingOption
import com.qhack.application.services.openai.IOpenAIService
import com.qhack.application.services.property.SunnyScoreResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OfferServiceGenerateOfferCacheTest {

    private lateinit var offerRepo: FakeOfferRepository
    private lateinit var customerRepo: FakeCustomerRepository
    private lateinit var propertyRepo: FakePropertyRepository
    private lateinit var productRepo: FakeProductRepository
    private lateinit var cacheRepo: FakeOfferLLMCacheRepository
    private lateinit var countingOpenAI: CountingOpenAIService
    private lateinit var service: OfferService

    @BeforeTest
    fun setup() {
        offerRepo = FakeOfferRepository()
        customerRepo = FakeCustomerRepository()
        propertyRepo = FakePropertyRepository()
        productRepo = FakeProductRepository()
        cacheRepo = FakeOfferLLMCacheRepository()
        countingOpenAI = CountingOpenAIService()
        service = OfferService(offerRepo, customerRepo, propertyRepo, productRepo, countingOpenAI, cacheRepo)
    }

    @Test
    fun `generateOffer caches by request json hash`() = runBlocking {
        // Arrange minimal domain prerequisites (not strictly needed for this test)
        val customerId = customerRepo.addCustomer(CustomerData("John", "Doe"))
        val propertyId = propertyRepo.addProperty(PropertyData("10115", "Street", "City", "1", customerId), 80, "")
        productRepo.addProduct(ProductData("Solar Panel"))

        val request = OfferLLMRequest(
            propertyInfo = PropertyLLMInfo(
                postCode = "10115",
                street = "Friedrichstr.",
                houseNumber = "1",
                city = "Berlin",
                country = "Germany"
            ),
            customerProfile = CustomerLLMProfile(
                birthDate = "1980-01-01",
                metadata = mapOf("requested_product" to "Solar Panel")
            )
        )

        // Act: first call -> miss, second call -> hit
        val r1 = service.generateOffer(request)
        val r2 = service.generateOffer(request)

        // Assert
        assertEquals(1, countingOpenAI.calls)
        assertEquals(r1, r2)
    }

    private class CountingOpenAIService : IOpenAIService {
        var calls = 0
        private val fixed = dummyResponse()

        override suspend fun getSunnyScore(address: String): SunnyScoreResponse? = null

        override suspend fun generateOffer(request: OfferLLMRequest): OfferLLMResponse? {
            calls += 1
            return fixed
        }

        private fun dummyResponse(): OfferLLMResponse = OfferLLMResponse(
            leadSummary = LeadSummary(
                location = Location(
                    postalCode = "10115",
                    street = "Friedrichstr.",
                    houseNumber = "1",
                    city = "Berlin",
                    country = "Germany"
                ), primaryProducts = listOf("Solar Panel")
            ),
            marketContext = MarketContext(summary = "", drivers = listOf(), whyNow = listOf()),
            subsidies = listOf(Subsidy(name = "", status = "", relevance = "", estimatedEffectEur = 0.0, notes = "")),
            recommendedOffer = RecommendedOffer(
                packageName = "Paket A",
                products = listOf("Solar Panel"),
                reasoning = listOf(""),
                estimatedCostRangeEur = RangeEur(0, 0),
                estimatedAnnualSavingsEur = RangeEur(0, 0),
                estimatedPaybackYears = RangeDouble(0.0, 0.0)
            ),
            alternativeOffers = listOf(
                AlternativeOffer(
                    packageName = "Alt",
                    products = listOf(""),
                    positioning = "",
                    estimatedCostRangeEur = RangeEur(0, 0)
                )
            ),
            financingOptions = listOf(
                FinancingOption(
                    scenario = "Barzahlung",
                    downPaymentEur = 0,
                    loanAmountEur = 0,
                    termMonths = 0,
                    aprPercent = 0.0,
                    monthlyPaymentEur = 0,
                    notes = ""
                )
            ),
            salesTalkingPoints = listOf(),
            missingInformation = listOf(),
            disclaimer = ""
        )
    }
}
