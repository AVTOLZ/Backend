package dev.avt.api.admin.events

import dev.avt.api.admin.events.EventData.Companion.toEvent
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.AvailableHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.events() {
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

            post {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val body = call.receive<EventData>()

                transaction {
                    AvailableHoursTable.new {
                        this.requiredRank = body.requiredRank
                        this.startTime = body.startTime
                        this.endTime = body.endTime
                        this.title = body.title
                        this.description = body.description
                    }
                }
            }
        }
    }
}