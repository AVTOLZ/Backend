package dev.avt.database

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class AVTUser(
    val id: Int,
    val username: String,
    val password: String, // should be hashed with sha512
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val studentId: Int?
)
class UserService(database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val username = varchar("username", 50)
        val password = char("password", 128)
        val email = varchar("email", 50).nullable()
        val firstName = varchar("first_name", 50).nullable()
        val lastName = varchar("last_name", 50).nullable()
        val studentId = integer("student_id").nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: AVTUser): Int = dbQuery {
        Users.insert {
            it[username] = user.username
            it[password] = user.password
            it[email] = user.email
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[studentId] = user.studentId
        } get Users.id
    }

    suspend fun read(id: Int): AVTUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { AVTUser(
                    id = it[Users.id],
                    username = it[Users.username],
                    password = it[Users.password],
                    email = it[Users.email],
                    firstName = it[Users.firstName],
                    lastName = it[Users.lastName],
                    studentId = it[Users.studentId]
                ) }.singleOrNull()
        }
    }

    suspend fun update(id: Int, user: AVTUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[username] = user.username
                it[password] = user.password
                it[email] = user.email
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[studentId] = user.studentId
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}
