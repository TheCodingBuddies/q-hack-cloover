package com.qhack.application.controller.offer

import com.qhack.application.services.offer.OfferRequestDto
import com.qhack.application.services.offer.OfferResponseDto
import com.qhack.application.services.offer.OfferService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class OfferController(private val offerService: OfferService) {
    fun registerRoutes(route: Route) {
        route.route("/offers") {
            get {
                val offers = offerService.getAllOffers().map { (id, data) ->
                    OfferResponseDto.fromDomain(id, data)
                }
                call.respond(HttpStatusCode.OK, offers)
            }

            post {
                val request = call.receive<OfferRequestDto>()
                val id = offerService.createOffer(request.toDomain())
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid id")
                    return@get
                }
                val offer = offerService.getOfferById(id)
                if (offer == null) {
                    call.respond(HttpStatusCode.NotFound, "Offer not found")
                    return@get
                }
                call.respond(HttpStatusCode.OK, OfferResponseDto.fromDomain(id, offer))
            }
        }
    }
}
