package dev.avt.database

import dev.avt.database.UserService.Users.nullable
import dev.avt.util.getRandomString
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

class BearerToken(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BearerToken>(BearerService.Bearer)
    var bearerToken by BearerService.Bearer.bearerToken
    var user by AVTUser referencedOn BearerService.Bearer.user
}

fun AVTUser.createBearerToken(): String {
    val bearer = getRandomString(128)

    BearerService.Bearer.insertAndGetId {
        it[this.user] = this@createBearerToken.id
        it[this.bearerToken] = bearer
    }

    return bearer
}

class BearerService(database: Database) {
    object Bearer : IntIdTable() {
        val bearerToken = varchar("bearer_token", 128)
        val user = reference("user", UserService.Users)
    }
    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Bearer)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }



    companion object {
        lateinit var INSTANCE: BearerService

        fun find(id: Int) = Bearer.select { Bearer.id eq id }.map { result ->
            BearerToken(result[Bearer.id]).also {
                it.bearerToken = result[Bearer.bearerToken]
            }
        }.single()
    }
}
