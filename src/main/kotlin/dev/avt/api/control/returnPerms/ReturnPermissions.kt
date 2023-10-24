package dev.avt.api.control.returnPerms

import dev.avt.database.AVTUser
import dev.avt.database.BearerService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.returnPermissionsRoutes(){
    post("/api/control/ReturnPerms") {
        val body = call.receive<ReturnPermissionsRequest>()

        val user = null // here goes the code that checks what user it is and if bearer toke is valid

        // here goes code to check permissions the user has

        // here goes code to return the permissions
    }
}