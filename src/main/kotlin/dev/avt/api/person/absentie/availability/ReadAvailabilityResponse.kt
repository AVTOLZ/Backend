package dev.avt.api.person.absentie.availability

import kotlinx.serialization.Serializable


// TODO this isnt sending enough data yet, it needs start and end time
@Serializable
data class ReadAvailabilityResponse(val hours: Array<Int>)