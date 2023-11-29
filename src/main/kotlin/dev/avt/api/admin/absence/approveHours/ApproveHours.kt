package dev.avt.api.admin.absence.approveHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.approveHoursRoute() {
    route("/api/admin/{peronId}/approve_absence") {
        authenticate("auth-bearer") {
            post {
                // TODO test this
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<ApproveHourRequest>()

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

                val approvedHour = transaction {
                    UserHoursTable.find {
                        (UserHoursService.UserHours.id eq body.id)
                    }.firstOrNull()
                }

                if (approvedHour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                if (approvedHour.presentType == PresenceType.PRESENT || approvedHour.approved){
                    call.respond(HttpStatusCode.Conflict)
                    return@post
                }

                transaction {
                    approvedHour.approved = true
                    approvedHour.approver = reqUser
                    approvedHour.timeApproved = Clock.System.now().epochSeconds
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}