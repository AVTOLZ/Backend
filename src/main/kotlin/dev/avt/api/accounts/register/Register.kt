package dev.avt.api.accounts.register

import dev.avt.Email
import dev.avt.api.accounts.login.LoginRequest
import dev.avt.api.accounts.login.LoginResponse
import dev.avt.database.AVTUser
import dev.avt.database.UserService
import dev.avt.database.UserService.Users.username
import dev.avt.database.UserState
import dev.avt.database.createBearerToken
import dev.tiebe.magisterapi.api.account.LoginFlow
import dev.tiebe.magisterapi.api.account.ProfileInfoFlow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

val verificationCodes = mutableMapOf<Int, Int>()

fun Routing.registerRoutes() {
    post("/api/accounts/register") {
        val body = call.receive<RegisterRequest>()

        val user = transaction {
            AVTUser.new {
                this.userName = body.username
                this.password = body.password
                this.email = body.email
                this.firstName = body.firstName
                this.lastName = body.lastName
            }
        }

        verificationCodes[user.id.value] = (100000..999999).random()

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