package com.qhack.application.controller.property

import com.qhack.application.domain.property.PropertyData
import com.qhack.application.services.openai.OpenAIService
import com.qhack.application.services.openai.OpenAIServiceMock
import com.qhack.application.services.property.PropertyRequestDto
import com.qhack.application.services.property.PropertyResponseDto
import com.qhack.application.services.property.PropertyService
import io.ktor.http.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PropertyController(private val propertyService: PropertyService, private val openAiService: OpenAIServiceMock) {
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
                    val response = openAiService.getSunnyScore("${data.street}, ${data.houseNumber}, ${data.city}, ${data.postCode}")
                    call.respond(HttpStatusCode.OK, PropertyResponseDto(id, response))
                } catch (e: NotFoundException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Customer not found")
                }
            }
        }
    }
}