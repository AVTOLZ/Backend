package dev.avt.api.admin.absence.denyHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.denyHoursRoute() {
    route("api/admin/{personId}/deny_hours") {
        authenticate("auth-bearer") {
            post {
                // TODO test this
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<DenyHoursRequest>()

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                if (reqUser.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val deniedHour = transaction {
                    UserHoursTable.find {
                        UserHoursService.UserHours.id eq body.id
                    }.firstOrNull()
                }

                if (deniedHour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                if (deniedHour.presentType != PresenceType.Absence || deniedHour.approved) {
                    call.respond(HttpStatusCode.Conflict)
                    return@post
                }

                transaction { deniedHour.delete() }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}