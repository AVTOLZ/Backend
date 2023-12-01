package dev.avt.api.person.absence.present

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

fun Routing.announcePresenceRouting(){
    route("/api/person/{personId}/announce_presence") {
        authenticate("auth-bearer") {
            post {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<AnnouncePresenceRequest>()

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val requestedHour = transaction { AvailableHoursTable.find { AvailableHoursService.AvailableHours.id eq body.hour }.firstOrNull() }

                if (requestedHour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                if (body.remove) {
                    val removeHour = transaction {
                        UserHoursTable.find {
                            (UserHoursService.UserHours.user eq reqUser.id.value) and (UserHoursService.UserHours.hour eq requestedHour.id.value)
                        }.firstOrNull()
                    }

                    if (removeHour == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    if (removeHour.approved){
                        call.respond(HttpStatusCode.Conflict)
                        return@post
                    }

                    transaction { removeHour.delete() }
                    call.respond(HttpStatusCode.OK)
                    return@post
                }

                val notNiceClientCheck = transaction {
                    UserHoursTable.find {
                        (UserHoursService.UserHours.user eq reqUser.id) and (UserHoursService.UserHours.hour eq requestedHour.id.value)
                    }.firstOrNull()
                }

                if (notNiceClientCheck != null) {
                    if (notNiceClientCheck.approved) {
                        call.respond(HttpStatusCode.Conflict)
                        return@post
                    }

                    if (notNiceClientCheck.presentType == PresenceType.PRESENT){
                        call.respond(HttpStatusCode.OK)
                        return@post
                    }

                    if (notNiceClientCheck.presentType == PresenceType.ABSENCE) {
                        transaction {
                            notNiceClientCheck.presentType = PresenceType.PRESENT
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

                transaction {
                    UserHoursTable.new {
                        this.user = reqUser
                        this.hour = requestedHour
                        this.presentType = PresenceType.PRESENT
                        this.timeRequested = Clock.System.now().epochSeconds
                    }
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}