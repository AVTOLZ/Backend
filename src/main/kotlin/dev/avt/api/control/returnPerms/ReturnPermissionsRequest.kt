package dev.avt.api.control.returnPerms

import kotlinx.serialization.Serializable

@Serializable
data class ReturnPermissionsRequest(val id: Int, val token: String)