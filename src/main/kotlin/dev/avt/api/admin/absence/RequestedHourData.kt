package dev.avt.api.admin.absence

import dev.avt.database.PresenceType
import dev.avt.database.State
import dev.avt.database.UserHoursTable
import kotlinx.serialization.Serializable

@Serializable
data class RequestedHourData(
    val userId: Int,
    val hourId: Int,
    val approver: Int?,
    val timeApproved: Long?,
    val presenceType: PresenceType,
    val state: State,
    val timeRequested: Long
) {
    companion object {
        fun UserHoursTable.toRequestedHourData(): RequestedHourData {
            return RequestedHourData(
                this.user.id.value,
                this.hour.id.value,
                this.approver?.id?.value,
                this.timeApproved,
                this.presentType,
                this.state,
                this.timeRequested
            )
        }

    }
}
