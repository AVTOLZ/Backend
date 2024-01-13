package dev.avt.api

import dev.avt.api.accounts.accountRouting
import dev.avt.api.admin.adminRouting
import dev.avt.api.person.magister.magisterRouting
import dev.avt.api.person.permissions.permissionRouting
import dev.avt.api.person.personRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.apiRouting() {
    accountRouting()
    magisterRouting()
    permissionRouting()
    personRouting()
    adminRouting()

    route("/test") {
        get {
            call.respond(HttpStatusCode.OK)
        }
    }
}