package dev.avt.api.person.absence.requestHours

import kotlinx.serialization.Serializable

@Serializable
data class RequestHoursRequest(val token: String, val hours: List<Int>)