package dev.avt.api.accounts.register

import at.favre.lib.crypto.bcrypt.BCrypt
import dev.avt.Email
import dev.avt.api.accounts.login.LoginResponse
import dev.avt.database.AVTUser
import dev.avt.database.UserService
import dev.avt.database.UserState
import dev.avt.database.createBearerToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

val verificationCodes = mutableMapOf<Int, String>()

fun Routing.registerRoutes() {
    post("/api/accounts/register") {
        val body = call.receive<RegisterRequest>()

        val existingUser = transaction {
            AVTUser.find {
                (UserService.Users.username eq body.username) or (UserService.Users.email eq body.email)
            }.firstOrNull()
        }

        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val user = transaction {
            AVTUser.new {
                this.userName = body.username
                this.password = BCrypt.withDefaults().hashToString(12, body.password.toCharArray())
                this.email = body.email
                this.firstName = body.firstName
                this.lastName = body.lastName
            }
        }

        verificationCodes[user.id.value] = String.format("%06d", Random.nextInt(999999));

        Email.sendMail(body.email, "Welcome to AVT!", """
            Hello ${user.firstName},
            
            Welcome to AVT!
            
            Your username is: ${user.userName}
            
            Please enter the following code to verify your account: ${verificationCodes[user.id.value]}
        """.trimIndent())


        val bearer = transaction {
            user.createBearerToken()
        }

        call.respond(HttpStatusCode.OK, LoginResponse(user.id.value, bearer, user.state == UserState.UNVERIFIED))
    }
}