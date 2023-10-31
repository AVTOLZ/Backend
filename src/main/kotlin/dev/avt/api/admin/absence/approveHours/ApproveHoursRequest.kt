package dev.avt.api.admin.absence.approveHours

import kotlinx.serialization.Serializable

@Serializable
data class ApproveHoursRequest(val token: String, val hours: List<Int>)