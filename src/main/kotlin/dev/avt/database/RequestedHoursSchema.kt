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

class RequestedHoursTable(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RequestedHoursTable>(RequestedHoursService.RequestedHours)
    var user by AVTUser referencedOn RequestedHoursService.RequestedHours.user
    var hour by AvailableHoursTable referencedOn RequestedHoursService.RequestedHours.hour
}

class RequestedHoursService(database: Database) {

    object RequestedHours : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hour = reference("hour", AvailableHoursService.AvailableHours)
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(RequestedHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: RequestedHoursService
    }
}