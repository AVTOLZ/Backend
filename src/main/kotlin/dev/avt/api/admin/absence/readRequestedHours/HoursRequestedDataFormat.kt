package dev.avt.api.admin.absence.readRequestedHours

import kotlinx.serialization.Serializable

// TODO add magister grade and test data
@Serializable
data class HoursRequestedDataFormat(val user: String, val startTime: Long, val endTime: Long, val id: Int)