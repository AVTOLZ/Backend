package dev.avt.plugins

import dev.avt.database.*
import dev.avt.database.UserService.Users.username
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

lateinit var database: Database

fun Application.configureDatabases() {
    database = Database.connect(
        url = "jdbc:mariadb://86.83.65.7:3306/main",
        user = "dev",
        driver = "org.mariadb.jdbc.Driver",
        password = "AVT123456!!"
    )

    val userService = UserService(database)
    val bearerService = BearerService(database)
    val approvedHoursService = ApprovedHoursService(database)
    val availableHoursService = AvailableHoursService(database)
    val requestedHoursService = RequestedHoursService(database)

    UserService.INSTANCE = userService
    BearerService.INSTANCE = bearerService
    ApprovedHoursService.INSTANCE = approvedHoursService
    AvailableHoursService.INSTANCE = availableHoursService
    RequestedHoursService.INSTANCE = requestedHoursService
}
