package dev.avt.api.control

import dev.avt.api.control.returnPerms.returnPermissionsRoutes
import io.ktor.server.routing.*

fun Routing.controlRouting(){
    returnPermissionsRoutes()
}