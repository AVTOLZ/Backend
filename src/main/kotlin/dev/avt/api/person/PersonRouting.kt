package dev.avt.api.person

import dev.avt.api.person.absence.absenceRouting
import dev.avt.api.person.info.readInfo
import dev.avt.api.person.magister.magisterRouting
import io.ktor.server.routing.*

fun Routing.personRouting() {
    absenceRouting()
    magisterRouting()
    readInfo()
}