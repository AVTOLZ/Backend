package dev.avt.api

import dev.avt.api.accounts.accountRouting
import dev.avt.api.person.magister.magisterRouting
import io.ktor.server.routing.*

fun Routing.apiRouting() {
    accountRouting()
    magisterRouting()
}