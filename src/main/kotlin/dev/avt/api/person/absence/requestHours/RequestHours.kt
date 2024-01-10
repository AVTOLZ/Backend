package dev.avt.api.person.absence.requestHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
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
                    UserHoursTable.find {
                        (UserHoursService.UserHours.user eq reqUser.id) and (UserHoursService.UserHours.hour eq requestedHour.id.value)
                    }.firstOrNull()
                }

                if (notNiceClientCheck != null){
                    if (notNiceClientCheck.approved) {
                        call.respond(HttpStatusCode.Conflict)
                        return@post
                    }

                    if (notNiceClientCheck.presentType == PresenceType.ABSENCE) {
                        call.respond(HttpStatusCode.OK)
                        return@post
                    }

                    if (notNiceClientCheck.presentType == PresenceType.PRESENT){
                        transaction {
                            notNiceClientCheck.presentType = PresenceType.ABSENCE
                            notNiceClientCheck.approved = false
                            notNiceClientCheck.approver = null
                            notNiceClientCheck.timeApproved = null
                            notNiceClientCheck.timeRequested = Clock.System.now().epochSeconds
                        }
                        call.respond(HttpStatusCode.OK)
                        return@post
                    }
                }

                if (!reqUser.rank.ge(requestedHour.requiredRank)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                transaction { UserHoursTable.new {
                    this.user = reqUser
                    this.hour = requestedHour
                    this.presentType = PresenceType.ABSENCE
                    this.timeRequested = Clock.System.now().epochSeconds
                }}

                call.respond(HttpStatusCode.OK)

                // TODO ask tiebe how to add shit to server.yaml
                // note to self: I honestly fear testing and debugging this code for bugs :)
            }

            delete {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@delete
                val body = call.receive<RequestHoursRequest>()

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete
                }

                val requestedHour = transaction { AvailableHoursTable[body.hour] }

                val removeHour = transaction {
                    UserHoursTable.find {
                        (UserHoursService.UserHours.user eq reqUser.id) and (UserHoursService.UserHours.hour eq requestedHour.id.value)
                    }.firstOrNull()
                }

                if (removeHour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                if (removeHour.approved) {
                    call.respond(HttpStatusCode.Conflict)
                    return@delete
                }

                transaction { removeHour.delete() }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}