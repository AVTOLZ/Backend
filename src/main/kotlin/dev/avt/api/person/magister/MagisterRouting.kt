package dev.avt.api.person.magister

import io.ktor.server.routing.*

fun Routing.magisterRouting() {
    linkRouting()
    classRouting()
}