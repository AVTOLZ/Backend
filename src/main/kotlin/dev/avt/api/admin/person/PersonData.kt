package dev.avt.api.admin.person

import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.UserState
import kotlinx.serialization.Serializable

@Serializable
data class PersonData (
    val id: Int,
    val username: String,
    val password: String?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val studentId: Int,
    val rank: AVTRanks,
    val state: UserState
) {
    companion object {
        fun AVTUser.toPersonData(): PersonData {
            return PersonData(
                this.id.value,
                this.userName,
                null,
                this.email,
                this.firstName,
                this.lastName,
                this.studentId,
                this.rank,
                this.state
            )
        }
    }
}