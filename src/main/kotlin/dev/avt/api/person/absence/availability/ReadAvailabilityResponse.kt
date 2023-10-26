package dev.avt.api.person.absence.availability

import kotlinx.serialization.Serializable

@Serializable
data class ReadAvailabilityResponse(val hours: List<HourDataFormat>)