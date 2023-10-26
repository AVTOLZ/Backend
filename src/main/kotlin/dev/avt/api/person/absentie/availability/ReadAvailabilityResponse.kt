package dev.avt.api.person.absentie.availability

import kotlinx.serialization.Serializable

@Serializable
data class ReadAvailabilityResponse(val hours: Array<HourDataFormat>)

@Serializable
data class HourDataFormat(val id: Int, val startTime: Long, val endTime: Long)