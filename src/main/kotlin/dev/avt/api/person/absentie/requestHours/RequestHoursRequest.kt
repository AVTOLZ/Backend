package dev.avt.api.person.absentie.requestHours

import kotlinx.serialization.Serializable

@Serializable
data class RequestHoursRequest(val token: String, val hours: List<Int>)