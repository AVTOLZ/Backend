package dev.avt.api.admin.person

import dev.avt.api.admin.person.PersonData.Companion.toPersonData
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.person() {
    route("/api/admin/person") {
        authenticate("auth-bearer") {
            get {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val userList = transaction {
                    AVTUser.all().map { it.toPersonData() }
                }

                call.respond(HttpStatusCode.OK, userList)
            }
        }
    }
}