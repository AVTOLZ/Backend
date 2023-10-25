package dev.avt.api.accounts

import dev.avt.api.accounts.login.loginRoutes
import dev.avt.api.accounts.register.registerRoutes
import io.ktor.server.routing.*

fun Routing.accountRouting() {
    registerRoutes()
    loginRoutes()
}