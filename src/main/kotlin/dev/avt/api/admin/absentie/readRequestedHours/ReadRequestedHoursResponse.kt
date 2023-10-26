package dev.avt.api.admin.absentie.readRequestedHours

import dev.avt.api.admin.absentie.approveHours.HoursRequestedDataFormat
import kotlinx.serialization.Serializable

@Serializable
data class ReadRequestedHoursResponse(val requests: List<HoursRequestedDataFormat>)