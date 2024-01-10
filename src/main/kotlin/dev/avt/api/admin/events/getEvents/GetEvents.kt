package dev.avt.api.admin.events.getEvents

import dev.avt.api.admin.events.getEvents.Event.Companion.toEvent
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.AvailableHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.getEvents() {
    route("/api/admin/events") {
        authenticate("auth-bearer") {
            get {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val requestedHoursList = transaction {
                    AvailableHoursTable.all().map { it.toEvent() }
                }

                call.respond(HttpStatusCode.OK, requestedHoursList)
            }
        }
    }
}