package com.qhack.application.controller.customer

import com.qhack.application.domain.customer.CustomerData
import com.qhack.application.services.customer.CustomerRequestDto
import com.qhack.application.services.customer.CustomerResponseDto
import com.qhack.application.services.customer.CustomerService
import com.qhack.application.services.customer.CustomerWithPropertiesResponseDto
import com.qhack.application.services.property.PropertyResponseDto
import com.qhack.application.services.property.SunnyScoreResponse
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

        route.route("/customers-with-properties") {
            get {
                val data = customerService.getCustomersWithProperties()
                val response = data.map { (customerId, pair) ->
                    val (customer, properties) = pair
                    CustomerWithPropertiesResponseDto(
                        id = customerId,
                        firstName = customer.firstName,
                        lastName = customer.lastName,
                        birthDate = customer.birthDate,
                        properties = properties.map { (propertyId, propertyData) ->
                            PropertyResponseDto(
                                id = propertyId,
                                postCode = propertyData.postCode,
                                street = propertyData.street,
                                city = propertyData.city,
                                houseNumber = propertyData.houseNumber,
                                sunnyScore = propertyData.sunnyScore?.let { score ->
                                    SunnyScoreResponse(
                                        address = "${propertyData.street} ${propertyData.houseNumber}, ${propertyData.postCode} ${propertyData.city}",
                                        sunnyPlace = score,
                                        explanation = "" // Explanation ist im PropertyData nicht enthalten, könnte man noch hinzufügen falls nötig
                                    )
                                }
                            )
                        }
                    )
                }
                call.respond(HttpStatusCode.OK, response)
            }
        }

        route.route("/customers/{id}/with-properties") {
            get {
                val customerId = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid customer id")

                val result = customerService.getCustomerWithProperties(customerId)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Customer with id $customerId not found")

                val (customer, properties) = result
                val response = CustomerWithPropertiesResponseDto(
                    id = customerId,
                    firstName = customer.firstName,
                    lastName = customer.lastName,
                    birthDate = customer.birthDate,
                    properties = properties.map { (propertyId, propertyData) ->
                        PropertyResponseDto(
                            id = propertyId,
                            postCode = propertyData.postCode,
                            street = propertyData.street,
                            city = propertyData.city,
                            houseNumber = propertyData.houseNumber,
                            sunnyScore = propertyData.sunnyScore?.let { score ->
                                SunnyScoreResponse(
                                    address = "${propertyData.street} ${propertyData.houseNumber}, ${propertyData.postCode} ${propertyData.city}",
                                    sunnyPlace = score,
                                    explanation = ""
                                )
                            }
                        )
                    }
                )
                call.respond(HttpStatusCode.OK, response)
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