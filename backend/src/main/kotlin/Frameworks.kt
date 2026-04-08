package com.qhack

import com.qhack.application.controller.customer.CustomerController
import com.qhack.application.controller.property.PropertyController
import com.qhack.application.infrastructure.customer.CustomerRepository
import com.qhack.application.infrastructure.customer.CustomerRepositoryImpl
import com.qhack.application.infrastructure.property.PropertyRepository
import com.qhack.application.infrastructure.property.PropertyRepositoryImpl
import com.qhack.application.services.customer.CustomerService
import com.qhack.application.services.property.PropertyService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<CustomerRepository> { CustomerRepositoryImpl() }
            single<PropertyRepository> { PropertyRepositoryImpl() }
            single<CustomerService> { CustomerService(get()) }
            single<PropertyService> { PropertyService(get(), get()) }
            single<CustomerController> { CustomerController(get()) }
            single<PropertyController> { PropertyController(get()) }
        })
    }
}
