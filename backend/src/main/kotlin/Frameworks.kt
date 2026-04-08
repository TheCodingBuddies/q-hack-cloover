package com.qhack

import com.qhack.application.controller.customer.CustomerController
import com.qhack.application.services.customer.CustomerService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<CustomerService>{CustomerService()}
            single<CustomerController>{CustomerController(get())}
        })
    }
}
