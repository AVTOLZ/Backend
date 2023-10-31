package dev.avt.api.admin.absence

import dev.avt.api.admin.absence.approveHours.approveHoursRoute
import dev.avt.api.admin.absence.readRequestedHours.readRequestedHours
import io.ktor.server.routing.*

fun Routing.adminAbsenceRouting() {
    approveHoursRoute()
    readRequestedHours()
}