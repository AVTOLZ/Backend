package dev.avt.api.accounts.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val personId: Int, val token: String)