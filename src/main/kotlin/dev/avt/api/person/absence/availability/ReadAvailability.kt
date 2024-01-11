package dev.avt.api.person.absence.availability

import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

                val remainingHours = transaction {
                    return@transaction AvailableHoursTable.all().toList()
                }

                if (remainingHours.isEmpty()) {
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                // also one of the main dissadvatages of an ide is that rn in vibing on the couch with my dog and my laptop on my lap, and dayum this thing is HOT
                // also also my dog took the spot i was sitting
                val allowedHours: MutableList<HourDataFormat> = mutableListOf()

                remainingHours.forEach {
                    if (reqUser.rank.ge(it.requiredRank)) {

                        allowedHours.add(HourDataFormat(
                            it.id.value,
                            it.startTime,
                            it.endTime,
                            it.title,
                            it.description,
                            checkPresentType(reqUser, it),
                            checkHourApproved(reqUser, it)))
                    }
                }

                if (allowedHours.isEmpty()) {
                    call.respond(HttpStatusCode.OK, ReadAvailabilityResponse(emptyList()))
                    return@get
                }

                call.respond(HttpStatusCode.OK, ReadAvailabilityResponse(allowedHours.toList()))
            }
        }
    }
}

fun checkHourApproved(reqUser: AVTUser, hourInQuestion: AvailableHoursTable): Boolean {
    val userHourEntry = transaction {
        UserHoursTable.find { (UserHoursService.UserHours.user eq reqUser.id.value) and (UserHoursService.UserHours.hour eq hourInQuestion.id.value) }.firstOrNull()
    } ?: return false

    return userHourEntry.state == State.APPROVED || userHourEntry.state == State.PROCESSED
}

fun checkPresentType(reqUser: AVTUser, hourInQuestion: AvailableHoursTable): PresenceType {
    val userHourEntry = transaction {
        UserHoursTable.find { (UserHoursService.UserHours.user eq reqUser.id.value) and (UserHoursService.UserHours.hour eq hourInQuestion.id.value) }.firstOrNull()
    } ?: return PresenceType.NOTHING

    return userHourEntry.presentType
}