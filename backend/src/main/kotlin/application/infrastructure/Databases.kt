package com.qhack.application.infrastructure

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database

fun startWebserver() {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
}

fun Application.configureDatabases() {
    val dotenv = dotenv()
    val jdbcUrl = "jdbc:postgresql://${dotenv["DATABASE_HOST"]}:${dotenv["DATABASE_PORT"]}/${dotenv["DATABASE_NAME"]}"
    val driver = "org.postgresql.Driver"
    val user = dotenv["DATABASE_USER"]
    val password = dotenv["DATABASE_PASSWORD"]

    Database.connect(
        url = jdbcUrl,
        driver = driver,
        user = user,
        password = password
    )
}
