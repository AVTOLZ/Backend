package dev.avt.api.admin

import dev.avt.api.admin.absence.adminAbsenceRouting
import dev.avt.api.admin.events.events
import dev.avt.api.admin.printing.printFile
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsenceRouting()
    events()
    printFile()
}