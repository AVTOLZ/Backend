package dev.avt.api.admin.absence

import kotlinx.serialization.Serializable

@Serializable
data class HourPatchRequest(val id: Int, val approved: Boolean)
