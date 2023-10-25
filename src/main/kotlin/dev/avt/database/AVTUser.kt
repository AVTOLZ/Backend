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
}

fun AVTUser.linkMagisterAccount() {

}