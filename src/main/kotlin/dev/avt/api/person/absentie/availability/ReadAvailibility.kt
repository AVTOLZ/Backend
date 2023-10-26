package dev.avt.api.person.absentie.availability

import dev.avt.database.AVTUser
import dev.avt.database.AvailableHoursService.AvailableHours.endTime
import dev.avt.database.AvailableHoursTable
import dev.avt.database.ge
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.readAvailabilityRoutes(){
    route("/api/person/{personId}/availability") {
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

                val currentDate = Clock.System.now()

                val remainingHours = transaction {
                    val allHours = AvailableHoursTable.find { endTime.greaterEq(currentDate.epochSeconds) }
                    return@transaction allHours.toList()
                }

                if (remainingHours.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                val allowedHours: Array<String> = emptyArray()

                remainingHours.forEach {
                    if (reqUser.rank.ge(it.requiredRank)) {
                        allowedHours + it.toString()
                    }
                }

                if (allowedHours.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                call.respond(HttpStatusCode.OK, ReadAvailabilityResponse(allowedHours))
            }
        }
    }
}
