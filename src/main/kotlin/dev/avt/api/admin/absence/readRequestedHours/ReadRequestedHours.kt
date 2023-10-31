package dev.avt.api.admin.absence.readRequestedHours

import dev.avt.database.AVTUser
import dev.avt.database.RequestedHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.readRequestedHours() {
    route("/api/admin/{personId}/requested_hours") {
        authenticate("auth-bearer") { 
            get {
                // TODO test this
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

                if (reqUser.rank.order != 0) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                if (RequestedHoursTable.all().empty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val res: MutableList<HoursRequestedDataFormat> = mutableListOf()

                RequestedHoursTable.all().forEach {
                    res.add(HoursRequestedDataFormat(it.user.userName, it.hour.startTime, it.hour.endTime, it.id.value))
                }

                call.respond(HttpStatusCode.OK, ReadRequestedHoursResponse(res.toList()))
            }
        }
    }
}