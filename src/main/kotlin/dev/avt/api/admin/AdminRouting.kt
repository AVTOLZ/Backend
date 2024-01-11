package dev.avt.api.admin

import dev.avt.api.admin.absence.adminAbsenceRouting
import dev.avt.api.admin.events.eventsRouting
import dev.avt.api.admin.printing.printFile
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsenceRouting()
    eventsRouting()
    printFile()
}