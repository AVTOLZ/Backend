package dev.avt.plugins

import dev.avt.database.*
import dev.avt.dotEnv
import org.jetbrains.exposed.sql.Database

lateinit var database: Database

fun configureDatabases() {
    database = Database.connect(
        url = "jdbc:mariadb://${dotEnv["DATABASE_URL"]}:3306/${dotEnv["DATABASE_NAME"]}",
        user = dotEnv["DATABASE_USERNAME"],
        driver = "org.mariadb.jdbc.Driver",
        password = dotEnv["DATABASE_PASSWORD"]
    )

    val userService = UserService(database)
    val bearerService = BearerService(database)
    val userHoursService = UserHoursService(database)
    val availableHoursService = AvailableHoursService(database)
    val magisterDataService = MagisterDataService(database)

    UserService.INSTANCE = userService
    BearerService.INSTANCE = bearerService
    UserHoursService.INSTANCE = userHoursService
    AvailableHoursService.INSTANCE = availableHoursService
    MagisterDataService.INSTANCE = magisterDataService
}
