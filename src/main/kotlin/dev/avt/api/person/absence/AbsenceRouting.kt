package dev.avt.api.person.absence

import dev.avt.api.person.absence.availability.readAvailabilityRoutes
import dev.avt.api.person.absence.requestHours.requestHours
import io.ktor.server.routing.*

fun Routing.absenceRouting(){
    readAvailabilityRoutes()
    requestHours()
}