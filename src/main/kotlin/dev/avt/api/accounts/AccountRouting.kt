package dev.avt.api.accounts

import dev.avt.api.accounts.login.loginRoutes
import dev.avt.api.accounts.register.registerRoutes
import dev.avt.api.accounts.verify.verificationRoutes
import io.ktor.server.routing.*

fun Routing.accountRouting() {
    registerRoutes()
    loginRoutes()
    verificationRoutes()
}