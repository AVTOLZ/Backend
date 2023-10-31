package dev.avt.api.admin.absence.readRequestedHours

import kotlinx.serialization.Serializable

@Serializable
data class ReadRequestedHoursResponse(val requests: List<HoursRequestedDataFormat>)