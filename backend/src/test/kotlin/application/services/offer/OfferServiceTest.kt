package com.qhack.application.services.offer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.domain.offer.OfferData
import com.qhack.application.domain.product.ProductData
import com.qhack.application.domain.property.PropertyData
import com.qhack.application.services.customer.FakeCustomerRepository
import com.qhack.application.services.product.FakeProductRepository
import com.qhack.application.services.property.FakePropertyRepository
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OfferServiceTest {

    private lateinit var offerRepo: FakeOfferRepository
    private lateinit var customerRepo: FakeCustomerRepository
    private lateinit var propertyRepo: FakePropertyRepository
    private lateinit var productRepo: FakeProductRepository
    private lateinit var service: OfferService

    @BeforeTest
    fun setup() {
        offerRepo = FakeOfferRepository()
        customerRepo = FakeCustomerRepository()
        propertyRepo = FakePropertyRepository()
        productRepo = FakeProductRepository()
        service = OfferService(offerRepo, customerRepo, propertyRepo, productRepo)
    }

    @Test
    fun `createOffer should succeed if all entities exist`(): Unit = runBlocking {
        // Given
        val customerId = customerRepo.addCustomer(CustomerData("John", "Doe"))
        val propertyId =
            propertyRepo.addProperty(PropertyData("12345", "Main St", "Berlin", "1", customerId), 80, "test")
        val productId = productRepo.addProduct(ProductData("Solar Panel"))

        val offerData = OfferData(customerId, propertyId, productId)

        // When
        val id = service.createOffer(offerData)

        // Then
        assertEquals(1, id)
        assertEquals(1, offerRepo.offersList.size)
        assertEquals(offerData, offerRepo.offersList[0].second)
    }

    @Test
    fun `createOffer should fail if customer does not exist`(): Unit = runBlocking {
        // Given
        val propertyId = 1
        val productId = 1
        val offerData = OfferData(99, propertyId, productId)

        // When / Then
        assertFailsWith<NotFoundException> {
            service.createOffer(offerData)
        }
    }

    @Test
    fun `createOffer should fail if property does not exist`(): Unit = runBlocking {
        // Given
        val customerId = customerRepo.addCustomer(CustomerData("John", "Doe"))
        val productId = 1
        val offerData = OfferData(customerId, 99, productId)

        // When / Then
        assertFailsWith<NotFoundException> {
            service.createOffer(offerData)
        }
    }

    @Test
    fun `createOffer should fail if product does not exist`(): Unit = runBlocking {
        // Given
        val customerId = customerRepo.addCustomer(CustomerData("John", "Doe"))
        val propertyId =
            propertyRepo.addProperty(PropertyData("12345", "Main St", "Berlin", "1", customerId), 80, "test")
        val offerData = OfferData(customerId, propertyId, 99)

        // When / Then
        assertFailsWith<NotFoundException> {
            service.createOffer(offerData)
        }
    }
}
