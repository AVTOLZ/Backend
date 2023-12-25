package dev.avt.api.person.permissions

import dev.avt.database.AVTUser
import dev.avt.database.PermissionList
import dev.avt.database.PermissionService
import dev.avt.database.PermissionService.Permissions.user
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction


// this function returns all permissions the user has
fun Routing.readPermissionsRoutes(){
    route("/api/person/{personId}/perms") {
        authenticate("auth-bearer") {
            get {
                val reqUser = call.principal<AVTUser>()
                val personId = call.parameters["personId"]?.toIntOrNull() ?: return@get

                if (reqUser == null){
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                if (reqUser.id.value != personId) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }

                val perms = transaction { PermissionList.find { user eq reqUser.id }.firstOrNull() }

                if (perms == null){
                    call.respond(HttpStatusCode.NoContent)
                    return@get
                }

                call.respond(HttpStatusCode.OK, ReadPermissionsResponse(
                    perms.hasAllPerms,
                    perms.hasFullLightAccess
                ))
            }
        }
    }
}