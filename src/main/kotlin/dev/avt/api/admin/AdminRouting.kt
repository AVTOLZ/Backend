package dev.avt.api.admin

import dev.avt.api.admin.absence.adminAbsenceRouting
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsenceRouting()
}