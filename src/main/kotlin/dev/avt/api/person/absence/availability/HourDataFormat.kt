package dev.avt.api.person.absence.availability

import dev.avt.database.PresenceType
import kotlinx.serialization.Serializable

@Serializable
data class HourDataFormat(val id: Int, val startTime: Long, val endTime: Long, val presentType: PresenceType?, val approved: Boolean)