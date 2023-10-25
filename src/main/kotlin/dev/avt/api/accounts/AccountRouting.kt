package dev.avt.api.accounts

import dev.avt.api.accounts.login.loginRoutes
import io.ktor.server.routing.*

fun Routing.accountRouting() {
    registerRoutes()
    loginRoutes()
}