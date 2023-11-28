package dev.avt.api.admin.absence

import dev.avt.api.admin.absence.approveHours.approveHoursRoute
import dev.avt.api.admin.absence.denyHours.denyHoursRoute
import dev.avt.api.admin.absence.readRequestedHours.readRequestedHours
import dev.avt.api.admin.absence.unapproveHours.unapproveHoursRoute
import io.ktor.server.routing.*

fun Routing.adminAbsenceRouting() {
    approveHoursRoute()
    denyHoursRoute()
    readRequestedHours()
    unapproveHoursRoute()
}