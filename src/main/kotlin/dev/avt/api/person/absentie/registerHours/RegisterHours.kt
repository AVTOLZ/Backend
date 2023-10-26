package dev.avt.api.person.absentie.registerHours

import dev.avt.database.AVTUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.Objects

fun Routing.registerHours(){
    route("/api/person/{personId}/submit_hours") {
        authenticate("auth-bearer") {
            post {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@post

                if (reqUser == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }

                val test: List<hourEntity> = [

                ]
            }
        }
    }
}

@Serializable
data class hourEntity(val startHour: Long, val endHour: Long)