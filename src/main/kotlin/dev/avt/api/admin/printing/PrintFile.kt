package dev.avt.api.admin.printing

import com.lowagie.text.Chunk
import com.lowagie.text.Document
import com.lowagie.text.FontFactory
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import dev.avt.database.AVTRanks
import dev.avt.database.AVTUser
import dev.avt.database.State
import dev.avt.database.UserHoursTable
import dev.avt.dotEnv
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

fun Routing.printFile() {
    route("/api/admin/print") {
        authenticate("auth-bearer") {
            post {
                val user = call.principal<AVTUser>()

                if (user?.rank != AVTRanks.Hoofd) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }
                val request = call.receive<PrintRequest>()

                val output = ByteArrayOutputStream()

                transaction {
                    val absences = request.requests.map {
                        val item = UserHoursTable.findById(it)
                        item?.state = State.PROCESSED

                        item
                    }

                    absences.generatePDF(output)
                }

                val printer = Printer()
                printer.login(dotEnv["PRINTER_ACCOUNT"], dotEnv["PRINTER_PASSWORD"])
                printer.print(output.toByteArray(), "absence.pdf")

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun List<UserHoursTable?>.generatePDF(outputStream: OutputStream) {
    val dayMap = mutableMapOf<LocalDate, MutableList<UserHoursTable>>()

    for (item in this) {
        if (item == null) continue

        val day = Instant.fromEpochSeconds(item.hour.startTime).toLocalDateTime(TimeZone.of("Europe/Amsterdam")).date
        if (dayMap.containsKey(day)) dayMap[day]!!.add(item)
        else dayMap[day] = mutableListOf(item)
    }

    val document = Document()
    PdfWriter.getInstance(document, outputStream)

    document.open()

    var addedInfo = false

    for ((day, hours) in dayMap) {
        document.add(Paragraph("Absentie Leerlingen door activiteit", FontFactory.getFont(FontFactory.HELVETICA, 14f)))
        document.add(Chunk.NEWLINE)

        document.add(generateTable(hours))
        document.add(Chunk.NEWLINE)

        document.add(Paragraph("Datum: ${day.year}-${day.monthNumber}-${day.dayOfMonth}", FontFactory.getFont(FontFactory.HELVETICA, 16f)))

        if (!addedInfo) {
            document.add(Paragraph("Extra commentaar:", FontFactory.getFont(FontFactory.HELVETICA, 10f)))
            document.add(Paragraph(
                """
                    De AVT, Audio Visuele Techniek, is de techniekcrew op het OLZ. 
                    Om shows te kunnen voorbereiden mogen leerlingen van de AVT lessen missen, 
                    mits ze geen toets hebben op dat moment en een voldoende staan 
                    voor het vak waarvan ze de les missen. 
                """.trimIndent().replace("\n", ""),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f)))
            document.add(Chunk.NEWLINE)
            document.add(Chunk.NEWLINE)


            document.add(Paragraph("Handtekeningen:", FontFactory.getFont(FontFactory.HELVETICA, 10f)))
            document.add(Paragraph("Getekend de Hoofd AVT en een leerlingcoördinator of teamleider.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8f)))

            document.add(Chunk.NEWLINE)

            // yes, i know, i hate it too. sadly, \t doesn't work here
            document.add(Paragraph("Naam: ${dotEnv["HOOFD_NAME"]}                                                      Naam:", FontFactory.getFont(FontFactory.HELVETICA, 8f)))

            addedInfo = true
        }

        document.newPage()
    }

    document.close()
}


fun generateTable(items: List<UserHoursTable>): PdfPTable {
    val userMap = mutableMapOf<AVTUser, MutableList<UserHoursTable>>()
    for (item in items) {
        if (userMap.containsKey(item.user)) userMap[item.user]!!.add(item)
        else userMap[item.user] = mutableListOf(item)
    }

    val nameSize = 0.28f
    val lnSize = 0.15f
    val boxSize = (1f - nameSize - lnSize) / 10f
    val columns = floatArrayOf(nameSize, lnSize, boxSize, boxSize, boxSize, boxSize, boxSize, boxSize, boxSize, boxSize, boxSize, boxSize)

    val table = PdfPTable(columns)

    table.addCell("Naam")
    table.addCell("LN")
    repeat(9) { table.addCell((it+1).toString()) }
    table.addCell("HD")

    table.completeRow()

    for ((user, registrations) in userMap) {
        table.addCell("${user.firstName} ${user.lastName}")
        table.addCell(user.studentId?.toString() ?: "")

        val notedHours = BooleanArray(9) { false }

        for (registration in registrations) {
            val hours = registration.getHours()

            hours.forEachIndexed { index, b ->
                if (b) notedHours[index] = true
            }
        }

        if (notedHours.size != 9) {
            for (hour in notedHours) {
                if (hour) table.addCell("X")
                else table.addCell("")
            }
        } else {
            repeat(9) {
                table.addCell("")
            }
            table.addCell("X")
        }

        table.completeRow()
    }

    return table
}

val timeMap = mapOf(
    (LocalTime(8, 30) to LocalTime(9, 15)) to 1,
    (LocalTime(9, 15) to LocalTime(10, 0)) to 2,
    (LocalTime(10, 20) to LocalTime(11, 5)) to 3,
    (LocalTime(11,5) to LocalTime(11, 50)) to 4,
    (LocalTime(12, 20) to LocalTime(13, 5)) to 5,
    (LocalTime(13, 5) to LocalTime(13, 50)) to 6,
    (LocalTime(14, 0) to LocalTime(14, 45)) to 7,
    (LocalTime(14, 45) to LocalTime(15, 30)) to 8,
    (LocalTime(15, 30) to LocalTime(16, 15)) to 9
)

fun UserHoursTable.getHours(): BooleanArray {
    val start = Instant.fromEpochSeconds(this.hour.startTime).toLocalDateTime(TimeZone.of("Europe/Amsterdam"))
    val end = Instant.fromEpochSeconds(this.hour.endTime).toLocalDateTime(TimeZone.of("Europe/Amsterdam"))

    val hours = BooleanArray(9)

    for ((times, hour) in timeMap) {
        hours[hour-1] = max(times.first.toSecondOfDay(),start.time.toSecondOfDay())<=min(times.second.toSecondOfDay(),end.time.toSecondOfDay())
    }

    return hours
}