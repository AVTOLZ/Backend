package dev.avt.plugins

import dev.avt.api.accounts.accountRouting
import dev.avt.api.apiRouting
import dev.avt.routing.authentication
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    install(DoubleReceive)
    install(Resources)
    routing {
        apiRouting()
    }
}

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
