package dev.avt.api.person.absentie.registerHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Objects

fun Routing.registerHours(){
    route("/api/person/{personId}/submit_hours") {
        authenticate("auth-bearer") {
            post {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post
                val body = call.receive<RegisterHoursRequest>()

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

                val requestedHours: List<AvailableHoursTable> = emptyList()

                body.hours.forEach {
                    val hourInQuestion = AvailableHoursTable[it]
                    if (!reqUser.rank.ge(hourInQuestion.requiredRank)) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@post
                    }
                    requestedHours + hourInQuestion
                }

                if (requestedHours.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                requestedHours.forEach {
                    RegisteredHoursTable.new {
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