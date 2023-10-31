package dev.avt.api.person.absence.requestHours

enum class HourRequestType(order: Int) {
    NOTHING(0),
    PRESENT(1),
    ABSENT(2) // absent in this context means the user is requesting absence
}