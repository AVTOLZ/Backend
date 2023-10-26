package dev.avt.api.admin.absentie

import dev.avt.api.admin.absentie.approveHours.approveHoursRoute
import dev.avt.api.admin.absentie.readRequestedHours.readRequestedHours
import io.ktor.server.routing.*

fun Routing.adminAbsentieRouting() {
    approveHoursRoute()
    readRequestedHours()
}