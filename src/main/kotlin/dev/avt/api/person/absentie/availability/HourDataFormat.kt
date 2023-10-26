package dev.avt.api.person.absentie.availability

import kotlinx.serialization.Serializable

@Serializable
data class HourDataFormat(val id: Int, val startTime: Long, val endTime: Long, val alreadyRegistered: Boolean)