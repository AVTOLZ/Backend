package dev.avt.api.admin.absentie.approveHours

import kotlinx.serialization.Serializable

@Serializable
data class ApproveHoursRequest(val token: String, val hours: List<Int>)