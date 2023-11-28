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

class UserHoursTable(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UserHoursTable>(UserHoursService.UserHours)
    var user by AVTUser referencedOn UserHoursService.UserHours.user
    var hour by AvailableHoursTable referencedOn UserHoursService.UserHours.hour
    var approver by AVTUser optionalReferencedOn  UserHoursService.UserHours.approver
    var timeApproved by UserHoursService.UserHours.timeApproved
    var presentType by UserHoursService.UserHours.PresentType
    var approved by UserHoursService.UserHours.approved
    var timeRequested by UserHoursService.UserHours.timeRequested
}

class UserHoursService(database: Database) {

    object UserHours : IntIdTable() {
        val user = reference("user", UserService.Users)
        val hour = reference("hour", AvailableHoursService.AvailableHours)
        val approver = reference("approver", UserService.Users).nullable()
        val timeApproved = long("time_approved").nullable()
        val PresentType = enumeration<PresenceType>("present_type")
        val approved = bool("approved").default(false)
        val timeRequested = long("time_requested")
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(UserHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: UserHoursService
    }
}

enum class PresenceType {
    Absence,
    Present
}