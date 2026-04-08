package com.qhack

import com.qhack.application.controller.customer.CustomerController
import com.qhack.application.controller.offer.OfferController
import com.qhack.application.controller.property.PropertyController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val customerController: CustomerController by inject()
    val propertyController: PropertyController by inject()
    val offerController: OfferController by inject()

    routing {
        customerController.registerRoutes(this)
        propertyController.registerRoutes(this)
        offerController.registerRoutes(this)
    }
}
