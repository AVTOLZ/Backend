package dev.avt.api.accounts.verify

import dev.avt.api.accounts.register.verificationCodes
import dev.avt.database.AVTUser
import dev.avt.database.UserState
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.loginRoutes() {
    post("/api/accounts/verify") {
        val code = call.parameters["code"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
        val personId = call.parameters["id"]?.toIntOrNull()  ?: return@post call.respond(HttpStatusCode.BadRequest)

        val user = transaction {
            AVTUser[personId]
        }

        if (code != verificationCodes[user.id.value]) {
            call.respond(HttpStatusCode.BadRequest)
        }

        transaction {
            user.state = UserState.VERIFIED
        }
    }
}