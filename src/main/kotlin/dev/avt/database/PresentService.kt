package dev.avt.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PresentTable(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PresentTable>(PresentService.PresentHours)
    var user by AVTUser referencedOn PresentService.PresentHours.user
    var hour by AvailableHoursTable referencedOn PresentService.PresentHours.hour
}

class PresentService(database: Database) {

    object PresentHours : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hour = reference("hour", AvailableHoursService.AvailableHours)
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(PresentHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: PresentService
    }
}