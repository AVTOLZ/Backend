package dev.avt.database

import io.ktor.server.auth.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

class AVTUser(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<AVTUser>(UserService.Users)
    var userName by UserService.Users.username
    var password by UserService.Users.password
    var email by UserService.Users.email
    var firstName by UserService.Users.firstName
    var lastName by UserService.Users.lastName
    var studentId by UserService.Users.studentId
}

class UserService(database: Database) {
    object Users : IntIdTable() {
        val username = varchar("username", 50)
        val password = char("password", 128)
        val email = varchar("email", 50).nullable()
        val firstName = varchar("first_name", 50).nullable()
        val lastName = varchar("last_name", 50).nullable()
        val studentId = integer("student_id").nullable()
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: UserService
    }
}
