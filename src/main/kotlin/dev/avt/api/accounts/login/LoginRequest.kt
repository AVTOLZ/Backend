package dev.avt.api.accounts.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)