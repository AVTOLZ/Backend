package dev.avt.api.person.absentie.registerHours

import kotlinx.serialization.Serializable

@Serializable
data class RegisterHoursRequest(val token: String, val hours: Array<Int>)