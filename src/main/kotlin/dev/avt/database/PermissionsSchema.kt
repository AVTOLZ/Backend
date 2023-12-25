package dev.avt.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

// This is the database table containing the permissions of all users
// It's also one of the reasons I want to commit die

class PermissionList(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PermissionList>(PermissionService.Permissions)
    var user by AVTUser referencedOn PermissionService.Permissions.user
    var hasAllPerms by PermissionService.Permissions.hasAllPerms
    var hasFullLightAccess by PermissionService.Permissions.hasFullLightAccess
}

fun AVTUser.createEmptyUserPermissions(){
    PermissionService.Permissions.insertAndGetId {
        it[this.user] = this@createEmptyUserPermissions.id
        it[this.hasAllPerms] = false
        it[this.hasFullLightAccess] = false
    }
}

class PermissionService(database: Database) {
    object Permissions : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hasAllPerms = bool("all_perms")
        val hasFullLightAccess = bool("full_light_access")
    }

    init {
        transaction(database) {
            SchemaUtils.create(Permissions)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: Permissions
    }
}

fun getPermissions(uid: EntityID<Int>): Unit? {
    val perms = transaction { PermissionList.find { PermissionService.Permissions.user eq uid }.firstOrNull() } ?: return null

    //TODO loops through all cells in the row perms and return an array of the column names of the cells that are true
    TODO("Provide the return value")
}