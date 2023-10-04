package dev.avt.plugins

import dev.avt.database.BearerService
import dev.avt.database.BearerToken
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureBearer() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "MainRealm"
            authenticate { tokenCredential ->
                //find all tokens that match the user supplied token
                val foundItems = BearerToken.find { BearerService.Bearer.bearerToken eq tokenCredential.token }

                return@authenticate foundItems.firstOrNull()?.user
            }
        }
    }
}