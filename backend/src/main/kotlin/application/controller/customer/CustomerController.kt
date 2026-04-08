package com.qhack.application.controller.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.services.customer.CustomerRequestDto
import com.qhack.application.services.customer.CustomerResponseDto
import com.qhack.application.services.customer.CustomerWithPropertyResponseDto
import com.qhack.application.services.customer.PropertyDto
import com.qhack.application.services.customer.CustomerService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


class CustomerController(private val customerService: CustomerService) {
    fun registerRoutes(route: Route) {
        route.route("/customers") {
            get {
                val customers = customerService.getAllCustomersWithProperties().map { (id, data, property) ->
                    CustomerWithPropertyResponseDto(
                        id = id,
                        firstName = data.firstName,
                        lastName = data.lastName,
                        birthDate = data.birthDate,
                        property = property?.let {
                            PropertyDto(
                                postCode = it.postCode,
                                street = it.street,
                                city = it.city,
                                houseNumber = it.houseNumber,
                                sunnyScore = it.sunnyScore,
                            )
                        }
                    )
                }
                call.respond(HttpStatusCode.OK, customers)
            }
        }

        route.route("/customer/{customerid}") {
            get {
                val customerId = call.parameters["customerid"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid customer ID")

                val result = customerService.getCustomerById(customerId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Customer not found")

                val (data, property) = result
                call.respond(
                    HttpStatusCode.OK,
                    CustomerWithPropertyResponseDto(
                        id = customerId,
                        firstName = data.firstName,
                        lastName = data.lastName,
                        birthDate = data.birthDate,
                        property = property?.let {
                            PropertyDto(
                                postCode = it.postCode,
                                street = it.street,
                                city = it.city,
                                houseNumber = it.houseNumber,
                                sunnyScore = it.sunnyScore,
                            )
                        }
                    )
                )
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
