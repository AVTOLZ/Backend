package dev.avt.api.person.absentie.availability

import dev.avt.database.AVTUser
import dev.avt.database.AvailableHoursService.AvailableHours.endTime
import dev.avt.database.AvailableHoursTable
import dev.avt.database.RegisteredHoursService.RegisteredHours.hour
import dev.avt.database.RegisteredHoursService.RegisteredHours.user
import dev.avt.database.RegisteredHoursTable
import dev.avt.database.ge
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
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

                // also one of the main dissadvatages of an ide is that rn in vibing on the couch with my dog and my laptop on my lap, and dayum this thing is HOT
                // also also my dog took the spot i was sitting
                val allowedHours: Array<HourDataFormat> = emptyArray()

                remainingHours.forEach {
                    if (reqUser.rank.ge(it.requiredRank)) {
                        val alreadyRegisteredHour = transaction {
                            RegisteredHoursTable.find { (user eq reqUser.id.value) and (hour eq it.id.value) }.firstOrNull()
                        }

                        var alreadyRegistered: Boolean = false

                        if (alreadyRegisteredHour != null) {
                            alreadyRegistered = true
                        }

                        allowedHours + HourDataFormat(it.id.value, it.startTime, it.endTime, alreadyRegistered)
                    }
                }

                if (allowedHours.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                call.respond(HttpStatusCode.OK, ReadAvailabilityResponse(allowedHours.toList()))
            }
        }
    }
}


