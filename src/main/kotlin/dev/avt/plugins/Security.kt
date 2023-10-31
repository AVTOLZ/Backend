package dev.avt.plugins

import dev.avt.database.BearerService
import dev.avt.database.BearerToken
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureSecurity() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "MainRealm"
            authenticate { tokenCredential ->
                //find all tokens that match the user supplied token
                return@authenticate transaction {
                    val foundItems = BearerToken.find { BearerService.Bearer.bearerToken eq tokenCredential.token }

                    return@transaction foundItems.firstOrNull()?.user
                }
            }
        }
    }
}
