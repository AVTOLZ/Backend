package dev.avt.api.person.absence.requestHours

enum class HourRequestType(order: Int) {
    Nothing(0),
    Present(1),
    Absent(2) // absent in this context means the user is requesting absence
}