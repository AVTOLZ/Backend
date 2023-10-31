package dev.avt.api.admin.absence.approveHours

import dev.avt.database.AVTUser
import dev.avt.database.ApprovedHoursTable
import dev.avt.database.RequestedHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock

fun Routing.approveHoursRoute() {
    route("/api/admin/{peronId}/approve_absence") {
        authenticate("auth-bearer") {
            post {
                // TODO test this
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<ApproveHoursRequest>()

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                if (reqUser.rank.order != 0) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                if (body.hours.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val approvedRequestedHours: Array<RequestedHoursTable> = emptyArray()

                body.hours.forEach {
                    approvedRequestedHours + RequestedHoursTable[it]
                }

                val currentTime = Clock.System.now().epochSeconds

                approvedRequestedHours.forEach {
                    ApprovedHoursTable.new {
                        user = it.user
                        hour = it.hour
                        approver = reqUser
                        timeApproved = currentTime
                    }

                    it.delete()
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}