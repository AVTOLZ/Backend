package dev.avt.api.person.absence.requestHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.requestHours(){
    route("/api/person/{personId}/request_hours") {
        authenticate("auth-bearer") {
            post {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<RequestHoursRequest>()

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val requestedHour = transaction { AvailableHoursTable[body.hour] }

                val notNiceClientCheck = transaction {
                    ApprovedHoursTable.find {
                        (ApprovedHoursService.ApprovedHours.user eq reqUser.id) and (ApprovedHoursService.ApprovedHours.hour eq requestedHour.id.value)
                    }.firstOrNull()
                }

                if (notNiceClientCheck != null) {
                    // if the client has already been approved it denies the request due to this not yet being implemented
                    call.respond(HttpStatusCode.NotImplemented)
                    return@post
                }

                if (body.requestType == HourRequestType.NOTHING) {
                    val removeHour = transaction {
                        RequestedHoursTable.find {
                            (RequestedHoursService.RequestedHours.user eq reqUser.id) and (RequestedHoursService.RequestedHours.hour eq requestedHour.id.value)
                        }.firstOrNull()
                    }
                    if (removeHour == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    transaction { removeHour.delete() }
                    call.respond(HttpStatusCode.OK)
                    return@post
                }

                if (body.requestType == HourRequestType.PRESENT) {
                    call.respond(HttpStatusCode.NotImplemented)
                    return@post
                }

                if (!reqUser.rank.ge(requestedHour.requiredRank)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                transaction { RequestedHoursTable.new {
                    this.user = reqUser
                    this.hour = requestedHour
                }}

                call.respond(HttpStatusCode.OK)

                // TODO ask tiebe how to add shit to server.yaml
                // note to self: I honestly fear testing and debugging this code for bugs :)
            }
        }
    }
}