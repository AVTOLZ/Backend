package dev.avt.database

import dev.avt.database.MagisterData.Companion.referrersOn
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class RegisteredHoursTable(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RegisteredHoursTable>(RegisteredHoursService.RegisteredHours)
    var user by AVTUser referencedOn RegisteredHoursService.RegisteredHours.user
    var hour by AvailableHoursTable referencedOn RegisteredHoursService.RegisteredHours.hour
}

class RegisteredHoursService(database: Database) {

    object RegisteredHours : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hour = reference("hour", AvailableHoursService.AvailableHours)
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(RegisteredHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: RegisteredHoursService
    }
}