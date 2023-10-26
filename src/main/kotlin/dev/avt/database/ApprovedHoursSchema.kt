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

class ApprovedHoursTable(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ApprovedHoursTable>(ApprovedHoursService.ApprovedHours)
    var user by AVTUser referencedOn ApprovedHoursService.ApprovedHours.user
    var hour by AvailableHoursTable referencedOn ApprovedHoursService.ApprovedHours.hour
    var approver by AVTUser referencedOn ApprovedHoursService.ApprovedHours.approver
    var timeApproved by ApprovedHoursService.ApprovedHours.timeApproved
}

class ApprovedHoursService(database: Database) {

    // I looked it up approver is a word in the english language but the spell check does not agree with me
    object ApprovedHours : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hour = reference("hour", AvailableHoursService.AvailableHours)
        val approver = reference("approver", UserService.Users)
        val timeApproved = long("time_approved")
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(ApprovedHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: ApprovedHoursService
    }
}