package dev.avt.api.person.absence.present

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncePresenceRequest(val hour: Int)