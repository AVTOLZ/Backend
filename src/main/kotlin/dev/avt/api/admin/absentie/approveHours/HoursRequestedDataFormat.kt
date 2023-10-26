package dev.avt.api.admin.absentie.approveHours

import kotlinx.serialization.Serializable

// TODO add magister grade and test data
@Serializable
data class HoursRequestedDataFormat(val user: String, val startTime: Long, val endTime: Long, val id: Int)