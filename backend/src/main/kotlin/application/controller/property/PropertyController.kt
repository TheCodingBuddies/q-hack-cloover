package com.qhack.application.controller.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.services.property.PropertyRequestDto
import com.qhack.application.services.property.PropertyService
import io.ktor.http.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PropertyController(private val propertyService: PropertyService) {
    fun registerRoutes(route: Route) {
        route.route("/customer/{customerid}/add-property") {
            post {
                val customerId = call.parameters["customerid"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid customer id")

                val request = call.receive<PropertyRequestDto>()
                val data = PropertyData(
                    postCode = request.postCode,
                    street = request.street,
                    city = request.city,
                    houseNumber = request.houseNumber,
                    customerId = customerId
                )

                try {
                    val id = propertyService.addProperty(data)
                    call.respond(HttpStatusCode.OK, "Property added with id: $id")
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Customer not found")
                }
            }
        }
    }
}