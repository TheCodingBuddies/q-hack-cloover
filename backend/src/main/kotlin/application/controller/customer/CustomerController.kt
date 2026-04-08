package com.qhack.application.controller.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.services.customer.CustomerRequestDto
import com.qhack.application.services.customer.CustomerResponseDto
import com.qhack.application.services.customer.CustomerService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


class CustomerController(private val customerService: CustomerService) {
    fun registerRoutes(route: Route) {
        route.route("/customers") {
            get {
                val customers = customerService.getAllCustomers().map { (id, data) ->
                    CustomerResponseDto(
                        id = id,
                        firstName = data.firstName,
                        lastName = data.lastName,
                        birthDate = data.birthDate
                    )
                }
                call.respond(HttpStatusCode.OK, customers)
            }
        }

        route.route("/add-customer") {
            options {
                call.respond(HttpStatusCode.OK)
            }
            post {
                val request = call.receive<CustomerRequestDto>()
                val data = CustomerData(
                    firstName = request.firstName,
                    lastName = request.lastName,
                    birthDate = request.birthDate
                )
                val id = customerService.addCustomer(data)
                call.respond(HttpStatusCode.OK, "Customer added with id: $id")
            }
        }
    }
}