package dev.avt.api.admin.absence

import dev.avt.api.admin.absence.RequestedHourData.Companion.toRequestedHourData
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.State
import dev.avt.database.UserHoursTable
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.absence() {
    route("/api/admin/absence") {
        authenticate("auth-bearer") {
            get {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val requestedHoursList = transaction {
                    UserHoursTable.all().map { it.toRequestedHourData() }
                }

                call.respond(HttpStatusCode.OK, requestedHoursList)
            }

            delete {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete
                }

                val id = call.receive<Int>()

                val hour = transaction { UserHoursTable.findById(id) }

                if (hour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                transaction { hour.delete() }

                call.respond(HttpStatusCode.OK)
            }

            patch {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@patch
                }

                val body = call.receive<HourPatchRequest>()

                val hour = transaction { UserHoursTable.findById(body.id) }

                if (hour == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@patch
                }

                if (body.approved) {
                    transaction {
                        hour.state = State.APPROVED
                        hour.approver = user
                        hour.timeApproved = Clock.System.now().epochSeconds
                    }

                    call.respond(HttpStatusCode.OK)
                    return@patch
                }

                else {
                    transaction {
                        hour.state = State.NONE
                        hour.approver = null
                        hour.timeApproved = null
                    }

                    call.respond(HttpStatusCode.OK)
                    return@patch
                }
            }
        }
    }
}