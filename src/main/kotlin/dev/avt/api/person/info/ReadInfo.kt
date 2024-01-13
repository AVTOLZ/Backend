package dev.avt.api.person.info

import dev.avt.database.AVTUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.readInfo() {
    route("/api/person/{personId}/info") {
        authenticate("auth-bearer") {
            get {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@get

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                call.respond(ReadInfoResponse(reqUser.userName, reqUser.firstName.toString(), reqUser.lastName.toString(), reqUser.rank))
            }
        }
    }
}