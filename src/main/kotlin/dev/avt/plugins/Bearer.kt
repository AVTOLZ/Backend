package dev.avt.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureBearer() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "Main_Realm"
            authenticate { tokenCredential ->

            }
        }
    }
}
// ...
data class UserSession(val state: String, val token: String)