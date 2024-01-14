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
// Koen was here
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

class UserService(database: Database) {
    object Users : IntIdTable() {
        val username = varchar("username", 50).uniqueIndex()
        val password = binary("password", 60)
        val email = varchar("email", 50).nullable()
        val firstName = varchar("first_name", 50).nullable()
        val lastName = varchar("last_name", 50).nullable()
        val studentId = integer("student_id")
        val rank = enumeration<AVTRanks>("user_rank").default(AVTRanks.Brugger)
        val state = enumeration<UserState>("state").default(UserState.UNVERIFIED)
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: UserService
    }
}
