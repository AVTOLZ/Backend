package dev.avt.api.admin

import dev.avt.api.admin.absence.absence
import dev.avt.api.admin.events.events
import dev.avt.api.admin.printing.printFile
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    absence()
    events()
    printFile()
}