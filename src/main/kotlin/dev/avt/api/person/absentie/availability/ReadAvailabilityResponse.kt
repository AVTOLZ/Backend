package dev.avt.api.person.absentie.availability

import kotlinx.serialization.Serializable

@Serializable
data class ReadAvailabilityResponse(val hours: Array<String>)