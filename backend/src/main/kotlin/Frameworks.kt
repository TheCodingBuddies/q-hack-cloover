package com.qhack

import com.qhack.application.controller.customer.CustomerController
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.customer.CustomerRepositoryImpl
import com.qhack.application.services.customer.CustomerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<CustomerRepository> { CustomerRepositoryImpl() }
            single<CustomerService> { CustomerService(get()) }
            single<CustomerController> { CustomerController(get()) }
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
