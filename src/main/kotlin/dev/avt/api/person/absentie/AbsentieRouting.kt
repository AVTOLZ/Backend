package dev.avt.api.person.absentie

import dev.avt.api.person.absentie.availability.readAvailabilityRoutes
import io.ktor.server.routing.*

fun Routing.absenceRouting(){
    readAvailabilityRoutes()
}