package dev.avt.api.person.absence.requestHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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

                if (body.hours.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val requestHours: Array<AvailableHoursTable> = emptyArray()

                body.hours.forEach {
                    val hourInQuestion = AvailableHoursTable[it]
                    if (!reqUser.rank.ge(hourInQuestion.requiredRank)) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@post
                    }
                    requestHours + hourInQuestion
                }

                if (requestHours.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                requestHours.forEach {
                    RequestedHoursTable.new {
                        user = reqUser
                        hour = it
                    }
                }

                call.respond(HttpStatusCode.OK)

                // TODO ask tiebe how to add shit to server.yaml
                // note to self: I honestly fear testing and debugging this code for bugs :)
            }
        }
    }
}