package dev.avt.api.admin.printing

import kotlinx.serialization.Serializable

@Serializable
data class PrintRequest(
    val requests: List<Int>
)
