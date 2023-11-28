package dev.avt.api.admin.absence.readRequestedHours

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

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

                if (reqUser.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val requestedHoursList = transaction {
                    UserHoursTable.find {
                        (UserHoursService.UserHours.PresentType eq PresenceType.Absence) and (UserHoursService.UserHours.approved eq false)
                    }.toList()
                }

                val responseList: MutableList<HoursRequestedDataFormat> = mutableListOf()

                requestedHoursList.forEach {
                    responseList.add(HoursRequestedDataFormat(
                        it.user.id.value,
                        it.hour.startTime,
                        it.hour.endTime,
                        it.timeRequested,
                        it.id.value
                    ))
                }

                call.respond(HttpStatusCode.OK, ReadRequestedHoursResponse(responseList.toList()))
            }
        }
    }
}