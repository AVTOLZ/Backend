package dev.avt.api.admin.absence.readRequestedHours

import kotlinx.serialization.Serializable

// TODO add magister grade and test data
@Serializable
data class HoursRequestedDataFormat(val user: Int, val startTime: Long, val endTime: Long, val timeRequested: Long, val id: Int)