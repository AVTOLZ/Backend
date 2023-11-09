package dev.avt.api.person.magister

import dev.avt.database.AVTUser
import dev.avt.database.MagisterDataService
import dev.avt.database.UserService
import dev.tiebe.magisterapi.api.account.LoginFlow
import dev.tiebe.magisterapi.api.account.ProfileInfoFlow
import dev.tiebe.magisterapi.response.TokenResponse
import dev.tiebe.magisterapi.utils.MagisterException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Routing.linkRouting() {
    route("/api/person/{personId}/magister") {
        authenticate("auth-bearer") {
            post {
                val personId = call.parameters["personId"]?.toIntOrNull()
                val user = call.principal<AVTUser>()

                if (personId == null || user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val refreshToken = call.receiveText()

                try {
                    // try to sign in and retrieve some basic user data. if that succeeds, the login info is valid.
                    val magisterTokens = LoginFlow.refreshToken(refreshToken)

                    val tenantUrl = ProfileInfoFlow.getTenantUrl(magisterTokens.accessToken)
                    val profileInfo = ProfileInfoFlow.getProfileInfo(tenantUrl.toString(), magisterTokens.accessToken)

                    // save magister information to database
                    transaction {
                        val avtUser = AVTUser[personId]

                        MagisterDataService.MagisterTable.insert {
                            it[this.user] = avtUser.id
                            it[this.accessToken] = magisterTokens.accessToken
                            it[this.refreshToken] = magisterTokens.refreshToken

                            it[this.tokenExpiry] = magisterTokens.expiresAt
                            it[this.tenantUrl] = tenantUrl.toString()
                        }
                    }

                    call.respond(HttpStatusCode.OK)
                } catch (e: MagisterException) {
                    // if login details are incorrect, tell client the entity sent was unprocessable.
                    call.respond(HttpStatusCode.UnprocessableEntity)
                }


            }
        }
    }
}