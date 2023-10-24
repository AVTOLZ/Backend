package dev.avt.api.control.returnPerms

import dev.avt.database.AVTUser
import dev.avt.database.UserService.Users.username
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.returnPermissionsRoutes(){
    get("/api/control/ReturnPerms") {
        val body = call.receive<ReturnPermissionsRequest>()

        // i wanna figure out a way to do bearer tokens with this but i dont know how so ill have to wait till tiebe explains it

        val user = transaction {
            val users = AVTUser.find {
                (username eq body.username)
            }

            users.firstOrNull()
        }

        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
        }

        // code to return perms of user here
    }
}