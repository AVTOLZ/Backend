package dev.avt.api.accounts.login

import dev.avt.database.*
import dev.avt.database.UserService.Users.id
import dev.avt.database.UserService.Users.password
import dev.avt.database.UserService.Users.username
import dev.tiebe.magisterapi.api.account.LoginFlow
import dev.tiebe.magisterapi.api.account.ProfileInfoFlow
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

        call.respond(HttpStatusCode.OK, LoginResponse(user.id.value, bearer))
    }

    post("/api/accounts/login/magister") {
        val refreshToken = call.receiveText()

        val magisterTokens = LoginFlow.refreshToken(refreshToken)

        val tenantUrl = ProfileInfoFlow.getTenantUrl(magisterTokens.accessToken)
        val profileInfo = ProfileInfoFlow.getProfileInfo(tenantUrl.toString(), magisterTokens.accessToken)

        val studyInfo = ProfileInfoFlow.getStudyInfo(tenantUrl.toString(), magisterTokens.accessToken, profileInfo.person.id)

        val user = transaction {
            AVTUser.find {
                UserService.Users.studentId eq studyInfo.stamNr.toIntOrNull()
            }.firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
            // Mr. Horseman was here
        }


        val bearer = transaction {
            user.createBearerToken()
        }

        call.respond(HttpStatusCode.OK, LoginResponse(user.id.value, bearer))
    }
}