package dev.avt.api.person.absence.availability

import dev.avt.database.*
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
                    println(reqUser.rank)
                    println(it.requiredRank)
                    println(reqUser.rank.ge(it.requiredRank))
                    if (reqUser.rank.ge(it.requiredRank)) {
                        val hourInQuestionStatus = checkHourStatus(reqUser, it)
                        val markedPresence = checkPresentAnnounced(reqUser, it)

                        allowedHours.add(HourDataFormat(it.id.value, it.startTime, it.endTime, hourInQuestionStatus, markedPresence))
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

fun checkHourStatus(reqUser: AVTUser, hourInQuestion: AvailableHoursTable): HourStatus {
    val approvedCheckThing = transaction {
        UserHoursTable.find { (UserHoursService.UserHours.user eq reqUser.id.value) and (UserHoursService.UserHours.hour eq hourInQuestion.id.value) }.firstOrNull()
    }

    if (approvedCheckThing != null) {
        return HourStatus.Approved
    }

    val requestedCheckThing = transaction {
        RequestedHoursTable.find { (RequestedHoursService.RequestedHours.user eq reqUser.id.value) and (RequestedHoursService.RequestedHours.hour eq hourInQuestion.id.value) }.firstOrNull()
    }

    if (requestedCheckThing != null) {
        return HourStatus.Requested
    }

    return  HourStatus.Open
}

fun checkPresentAnnounced(reqUser: AVTUser, hourInQuestion: AvailableHoursTable): Boolean {
    val present = transaction {
        PresentTable.find { (PresentService.PresentHours.user eq reqUser.id.value) and (PresentService.PresentHours.hour eq hourInQuestion.id.value) }.firstOrNull()
    }

    return present != null
}