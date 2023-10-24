package dev.avt.api.person.permissions

import kotlinx.serialization.Serializable

@Serializable
data class ReadPermissionsResponse(val hasAllPerms: Boolean, val hasFullLightAccess: Boolean)
