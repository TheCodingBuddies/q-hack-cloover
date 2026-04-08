package com.qhack

import com.qhack.application.controller.customer.CustomerController
import com.qhack.application.controller.offer.OfferController
import com.qhack.application.controller.property.PropertyController
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.customer.CustomerRepositoryImpl
import com.qhack.application.infrastructure.offer.OfferRepository
import com.qhack.application.infrastructure.offer.OfferRepositoryImpl
import com.qhack.application.infrastructure.product.ProductRepository
import com.qhack.application.infrastructure.product.ProductRepositoryImpl
import com.qhack.application.infrastructure.property.PropertyRepository
import com.qhack.application.infrastructure.property.PropertyRepositoryImpl
import com.qhack.application.services.customer.CustomerService
import com.qhack.application.services.offer.OfferService
import com.qhack.application.services.openai.OpenAIServiceMock
import com.qhack.application.services.property.PropertyService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single {
                HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(Json {
                            ignoreUnknownKeys = true
                            prettyPrint = true
                            isLenient = true
                            encodeDefaults = true
                        })
                    }
                }
            }
            single<CustomerRepository> { CustomerRepositoryImpl() }
            single<PropertyRepository> { PropertyRepositoryImpl() }
            single<ProductRepository> { ProductRepositoryImpl() }
            single<OfferRepository> { OfferRepositoryImpl() }
            single<OpenAIServiceMock> { OpenAIServiceMock(httpClient = get()) }
            single<CustomerService> { CustomerService(get()) }
            single<PropertyService> { PropertyService(get(), get()) }
            single<OfferService> { OfferService(get(), get(), get(), get()) }
            single<CustomerController> { CustomerController(get()) }
            single<PropertyController> { PropertyController(get(), get()) }
            single<OfferController> { OfferController(get()) }
        })
    }
    install(CORS) {
        allowHost("localhost:5173")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }
}
