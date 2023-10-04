package dev.avt.plugins

import dev.avt.database.BearerService
import dev.avt.database.BearerToken
import dev.avt.database.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.jetbrains.exposed.sql.select

fun Application.configureBearer() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "MainRealm"
            authenticate { tokenCredential ->
                val foundItems = BearerToken.find { BearerService.Bearer.bearerToken eq tokenCredential.token }

                for (item in foundItems) {
                    if (item.bearerToken == tokenCredential.token) {
                        return@authenticate item.user
                    }
                }

                null
            }
        }
    }
}
// ...
data class UserSession(val state: String, val token: String)