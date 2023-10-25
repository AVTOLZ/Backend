package dev.avt.database

import io.ktor.server.auth.*
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
}

fun AVTUser.linkMagisterAccount() {

}

enum class AVTRanks(val order: Int) {
    Brugger(0),
    PlannenLid(1),
    Hoofd(2)
}

fun AVTRanks.ge(other: AVTRanks) {
    this.order >= other.order
}

fun main() {
    val test = AVTRanks.Hoofd

    // i think this compares the order in which they're defined, which also works I suppose
    test >= AVTRanks.Brugger // no clue if this works

    test.ge(AVTRanks.Brugger) // this defintely works
}