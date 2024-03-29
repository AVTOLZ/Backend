package dev.avt.database

import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AVTUser(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<AVTUser>(UserService.Users)
    var userName by UserService.Users.username
    var password by UserService.Users.password
    var email by UserService.Users.email
    var firstName by UserService.Users.firstName
    var lastName by UserService.Users.lastName
    var studentId by UserService.Users.studentId
    var rank by UserService.Users.rank
    var state by UserService.Users.state
}

// this is currently mainly used for easily checking if a user is allowed to attend a certain thing based on rank
@Serializable
enum class AVTRanks(val order: Int) {
    Brugger(0),
    PlannenLid(1),
    Hoofd(2)
}

// this is a function to easily check if a users rank is sufficient for an event
// for usage example see ReadAvailability.kt
fun AVTRanks.ge(other: AVTRanks): Boolean {
    return this.order >= other.order
}

enum class UserState {
    UNVERIFIED, VERIFIED, DISABLED
}