package com.qhack

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureAdministration()
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
