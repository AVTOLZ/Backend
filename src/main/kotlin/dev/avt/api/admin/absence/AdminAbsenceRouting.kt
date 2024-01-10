package dev.avt.api.admin.absence

import dev.avt.api.admin.absence.RequestedHour.Companion.toRequestedHour
import dev.avt.api.admin.absence.approveHours.approveHoursRoute
import dev.avt.api.admin.absence.denyHours.denyHoursRoute
import dev.avt.api.admin.absence.readRequestedHours.readRequestedHours
import dev.avt.api.admin.absence.unapproveHours.unapproveHoursRoute
import dev.avt.database.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.adminAbsenceRouting() {
    approveHoursRoute()
    denyHoursRoute()
    readRequestedHours()
    unapproveHoursRoute()

    route("/api/admin/absences") {
        authenticate("auth-bearer") {
            get {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val requestedHoursList = transaction {
                    UserHoursTable.all().map { it.toRequestedHour() }
                }

                call.respond(HttpStatusCode.OK, requestedHoursList)
            }
        }

    }
}

@Serializable
data class RequestedHour(
    val userId: Int,
    val hourId: Int,
    val approver: Int?,
    val timeApproved: Long?,
    val presenceType: PresenceType,
    val state: State,
    val timeRequested: Long
) {
    companion object {
        fun UserHoursTable.toRequestedHour(): RequestedHour {
            return RequestedHour(
                this.user.id.value,
                this.hour.id.value,
                this.approver?.id?.value,
                this.timeApproved,
                this.presentType,
                this.state,
                this.timeRequested
            )
        }

    }
}