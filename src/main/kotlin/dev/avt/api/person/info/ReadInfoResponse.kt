package dev.avt.api.person.info

import dev.avt.database.AVTRanks
import kotlinx.serialization.Serializable

@Serializable
data class ReadInfoResponse(val username: String, val firstName: String, val lastName: String, val rank: AVTRanks)