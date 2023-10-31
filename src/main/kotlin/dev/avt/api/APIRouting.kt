package dev.avt.api

import dev.avt.api.accounts.accountRouting
import dev.avt.api.person.magister.magisterRouting
import dev.avt.api.person.personRouting
import io.ktor.server.routing.*

fun Routing.apiRouting() {
    accountRouting()
    personRouting()
}