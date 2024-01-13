package dev.avt

import dev.avt.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @BeforeTest
    fun setupDatabase() {
        val url = ApplicationTest::class.java.getResource("/database.h2") ?: throw NullPointerException()

        val database = Database.connect(
            url = "jdbc:h2:$url",
            driver = "org.h2.Driver",
        )

    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureSecurity()
            configureMonitoring()
            configureSerialization()
            configureSockets()
            configureRouting()
        }
        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testRoo2t() = testApplication {
        client.get("/test").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
