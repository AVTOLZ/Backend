package dev.avt.api.admin.events.getEvents

import dev.avt.database.AVTRanks
import dev.avt.database.AvailableHoursTable
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val requiredRank: AVTRanks,
    var startTime: Long,
    var endTime: Long,
    var title: String,
    var description: String
) {
    companion object {
        fun AvailableHoursTable.toEvent(): Event {
            return Event(
                this.requiredRank,
                this.startTime,
                this.endTime,
                this.title,
                this.description
            )
        }

    }
}