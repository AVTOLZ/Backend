package dev.avt.api.admin.events.addEvent

import dev.avt.api.admin.events.Event
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

fun Routing.addEvent() {
    route("/api/admin/events/add") {
        authenticate("auth-bearer") {
            post {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val body = call.receive<Event>()

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