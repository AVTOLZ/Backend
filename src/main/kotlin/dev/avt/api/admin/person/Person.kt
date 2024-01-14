package dev.avt.api.admin.person

import at.favre.lib.crypto.bcrypt.BCrypt
import dev.avt.api.admin.person.PersonData.Companion.toPersonData
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
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

            put {
                val user = call.principal<AVTUser>()

                val body = call.receive<PersonData>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }

                val entry = transaction { AVTUser.findById(body.id) }

                if (entry == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                transaction {
                    entry.userName = body.username
                    entry.firstName = body.firstName
                    entry.lastName = body.lastName
                    entry.email = body.email
                    entry.rank = body.rank
                    entry.state = body.state
                    entry.studentId = body.studentId
                    if (body.password != null) entry.password = BCrypt.withDefaults().hashToString(12, body.password.toCharArray()).toByteArray(Charsets.UTF_8)
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}