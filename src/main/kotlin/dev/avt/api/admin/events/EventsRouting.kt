package dev.avt.api.admin.events

import dev.avt.api.admin.events.getEvents.getEvents
import io.ktor.server.routing.*

fun Routing.eventsRouting() {
    getEvents()
}