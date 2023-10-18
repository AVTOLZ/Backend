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

class MagisterData(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MagisterData>(MagisterDataService.MagisterTable)
    var accessToken by MagisterDataService.MagisterTable.accessToken
    var refreshToken by MagisterDataService.MagisterTable.refreshToken

    var tokenExpiry by MagisterDataService.MagisterTable.tokenExpiry
    var tenantUrl by MagisterDataService.MagisterTable.tenantUrl

    var user by AVTUser referencedOn MagisterDataService.MagisterTable.user
}

class MagisterDataService(database: Database) {
    object MagisterTable : IntIdTable() {
        val accessToken = varchar("access_token", 256)
        val refreshToken = varchar("refresh_token", 256)

        val tokenExpiry = long("token_expiry")
        val tenantUrl = varchar("tenant_url", 128)

        val user = reference("user", UserService.Users)
    }
    init {
        transaction(database) {
            SchemaUtils.create(MagisterTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }



    companion object {
        lateinit var INSTANCE: BearerService
    }
}
