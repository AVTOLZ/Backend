package dev.avt.api.admin

import dev.avt.api.admin.absence.adminAbsenceRouting
import dev.avt.api.admin.events.events
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsenceRouting()
    events()
}