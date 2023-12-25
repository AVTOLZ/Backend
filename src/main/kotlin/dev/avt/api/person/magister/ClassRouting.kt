package dev.avt.api.person.magister

import dev.avt.database.AVTUser
import dev.avt.database.MagisterData
import dev.avt.database.MagisterDataService
import dev.tiebe.magisterapi.api.agenda.AgendaFlow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.Identity.decode
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.classRouting() {
    route("/api/person/{personId}/magister/classes") {
        authenticate("auth-bearer") {
            get {
                val personId = call.parameters["personId"]?.toIntOrNull()
                val user = call.principal<AVTUser>()

                if (personId == null || user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val magisterUser = transaction {
                    MagisterData.find { MagisterDataService.MagisterTable.user eq user.id }.firstOrNull()
                }

                if (magisterUser == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val startDate = call.parameters["start"] ?: call.respond(HttpStatusCode.BadRequest)
                val endDate = call.parameters["end"] ?: call.respond(HttpStatusCode.BadRequest)

                val classes = runBlocking {
                    AgendaFlow.getAgenda(magisterUser.tenantUrl, magisterUser.accessToken, magisterUser.id.value, startDate.toString(), endDate.toString())
                }

                call.respond(classes)
            }
        }
    }
}