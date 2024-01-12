package dev.avt.api.admin.absence

import dev.avt.api.admin.events.getEvents.EventData
import dev.avt.api.admin.events.getEvents.EventData.Companion.toEvent
import dev.avt.api.admin.person.PersonData
import dev.avt.api.admin.person.PersonData.Companion.toPersonData
import dev.avt.database.PresenceType
import dev.avt.database.State
import dev.avt.database.UserHoursTable
import kotlinx.serialization.Serializable

@Serializable
data class RequestedHourData(
    val userId: PersonData,
    val hourId: EventData,
    val approver: PersonData?,
    val timeApproved: Long?,
    val presenceType: PresenceType,
    val state: State,
    val timeRequested: Long
) {
    companion object {
        fun UserHoursTable.toRequestedHourData(): RequestedHourData {
            return RequestedHourData(
                this.user.toPersonData(),
                this.hour.toEvent(),
                this.approver?.toPersonData(),
                this.timeApproved,
                this.presentType,
                this.state,
                this.timeRequested
            )
        }

    }
}
