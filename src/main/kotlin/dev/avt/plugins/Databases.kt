package dev.avt.plugins

import dev.avt.database.*
import dev.avt.dotEnv
import org.jetbrains.exposed.sql.Database

fun configureDatabase() {
    val database = Database.connect(
        url = "jdbc:mariadb://${dotEnv["DATABASE_URL"]}:3306/${dotEnv["DATABASE_NAME"]}",
        user = dotEnv["DATABASE_USERNAME"],
        driver = "org.mariadb.jdbc.Driver",
        password = dotEnv["DATABASE_PASSWORD"]
    )

    setupTables(database)
}

fun setupTables(database: Database) {
    UserService(database)
    BearerService(database)
    UserHoursService(database)
    AvailableHoursService(database)
    MagisterDataService(database)
}