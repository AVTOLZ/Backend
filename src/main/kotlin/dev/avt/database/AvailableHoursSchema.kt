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

class AvailableHoursTable(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AvailableHoursTable>(AvailableHoursService.AvailableHours)
    var requiredRank by AvailableHoursService.AvailableHours.requiredRank
    var startTime by AvailableHoursService.AvailableHours.startTime
    var endTime by AvailableHoursService.AvailableHours.endTime
    var title by AvailableHoursService.AvailableHours.title
    var description by AvailableHoursService.AvailableHours.description
}

class AvailableHoursService(database: Database) {
    object AvailableHours : IntIdTable() {
        val requiredRank = enumeration<AVTRanks>("required_rank").default(AVTRanks.Brugger)
        val startTime = long("start_time").default(0)
        val endTime = long("end_time").default(0)
        val title = varchar("title", 50).default("default name")
        val description = varchar("description", 100).default("default description")
    }

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(AvailableHours)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var INSTANCE: AvailableHoursService
    }
}

enum class HourStatus(val order: Int) {
    Open(0),
    Requested(1),
    Approved(2)
}