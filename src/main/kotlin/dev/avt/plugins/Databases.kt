package dev.avt.plugins

import dev.avt.database.AVTUser
import dev.avt.database.BearerService
import dev.avt.database.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:mariadb://86.83.65.7:3306/main",
        user = "root",
        driver = "org.mariadb.jdbc.Driver",
        password = "AVT123456!!"
    )

    val userService = UserService(database)
    val bearerService = BearerService(database)

    UserService.INSTANCE = userService
    BearerService.INSTANCE = bearerService
}
