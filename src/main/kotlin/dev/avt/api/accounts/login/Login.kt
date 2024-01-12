package dev.avt.api.accounts.login

import at.favre.lib.crypto.bcrypt.BCrypt
import dev.avt.database.AVTUser
import dev.avt.database.UserService
import dev.avt.database.UserService.Users.password
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
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.loginRoutes() {
    post("/api/accounts/login") {
        val body = call.receive<LoginRequest>()

        val user = transaction {
            val users = AVTUser.find {
                username eq body.username
            }

            users.firstOrNull()
        }

        if (user == null || !BCrypt.verifyer().verify(body.password.toCharArray(), user.password).verified) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val bearer = transaction {
            user.createBearerToken()
        }

        call.respond(HttpStatusCode.OK, LoginResponse(user.id.value, bearer, user.state != UserState.UNVERIFIED))
    }

    post("/api/accounts/login/magister") {
        val refreshToken = call.receiveText()

        val magisterTokens = LoginFlow.refreshToken(refreshToken)

        val tenantUrl = ProfileInfoFlow.getTenantUrl(magisterTokens.accessToken)
        val profileInfo = ProfileInfoFlow.getProfileInfo(tenantUrl.toString(), magisterTokens.accessToken)

        val studyInfo = ProfileInfoFlow.getStudyInfo(tenantUrl.toString(), magisterTokens.accessToken, profileInfo.person.id)

        val user = transaction {
            AVTUser.find {
                UserService.Users.studentId eq studyInfo.stamNr.toInt()
            }.firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }


        val bearer = transaction {
            user.createBearerToken()
        }

        call.respond(HttpStatusCode.OK, LoginResponse(user.id.value, bearer, user.state == UserState.UNVERIFIED))
    }
}