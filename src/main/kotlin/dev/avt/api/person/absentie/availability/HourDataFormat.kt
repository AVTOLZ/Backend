package dev.avt.api.person.absentie.availability

import dev.avt.database.HourStatus
import kotlinx.serialization.Serializable

@Serializable
data class HourDataFormat(val id: Int, val startTime: Long, val endTime: Long, val status: HourStatus)