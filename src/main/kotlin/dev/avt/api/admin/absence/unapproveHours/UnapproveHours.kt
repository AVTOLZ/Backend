package dev.avt.api.admin.absence.unapproveHours

import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.UserHoursService
import dev.avt.database.UserHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.unapproveHoursRoute() {
    route("api/admin/{personId}/unapprove_absence") {
        authenticate("auth-bearer") {
            post {
                // TODO test this
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<UnapproveHoursRequest>()

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

                val unapprovedHour = transaction {
                    UserHoursTable.find {
                        UserHoursService.UserHours.id eq body.id
                    }.firstOrNull()
                }

                if (unapprovedHour == null){
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                if (!unapprovedHour.approved){
                    call.respond(HttpStatusCode.Conflict)
                    return@post
                }

                transaction {
                    unapprovedHour.approved = false
                    unapprovedHour.approver = null
                    unapprovedHour.timeApproved = null
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}