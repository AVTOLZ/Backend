package dev.avt.api.accounts.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val password: String, val email: String, val firstName: String, val lastName: String, val studentId: Int)
