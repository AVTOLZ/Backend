package dev.avt.api.admin

import dev.avt.api.admin.absentie.adminAbsentieRouting
import io.ktor.server.routing.*

fun Routing.adminRouting() {
    adminAbsentieRouting()
}