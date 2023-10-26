package dev.avt.api.admin.absentie.readRequestedHours

import kotlinx.serialization.Serializable

@Serializable
data class ReadRequestedHoursResponse(val requests: List<HoursRequestedDataFormat>)