package dev.avt.api.accounts.login

import dev.avt.database.AVTUser
import dev.avt.database.UserService
import dev.avt.database.UserService.Users.password
import dev.avt.database.UserService.Users.username
import dev.avt.database.createBearerToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.loginRoutes() {
    post("/api/accounts/login") {
        val body = call.receive<LoginRequest>()

        val user = transaction {
            val users = AVTUser.find {
                (username eq body.username) and (password eq body.password)
            }

            users.firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val bearer = transaction {
            user.createBearerToken()
        }

        call.respond(HttpStatusCode.OK, LoginResponse(bearer))
    }
}