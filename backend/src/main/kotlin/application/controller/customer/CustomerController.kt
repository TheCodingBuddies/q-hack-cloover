package com.qhack.application.controller.customer

import com.qhack.application.services.customer.CustomerService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class CustomerController(private val customerService: CustomerService) {
    fun registerRoutes(route: Route) {
        route.route("/add-customer") {
            post {
                customerService.addCustomer()
                call.respond(HttpStatusCode.OK, "ok cool")
            }
        }
    }
}