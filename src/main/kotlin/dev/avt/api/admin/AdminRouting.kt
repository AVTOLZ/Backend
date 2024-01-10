package dev.avt.api.admin

import dev.avt.api.admin.absence.adminAbsenceRouting
import dev.avt.api.admin.events.eventsRouting
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsenceRouting()
    eventsRouting()
}