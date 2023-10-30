package dev.avt.api.person.absence.requestHours

enum class HourRequestType(order: Int) {
    nothing(0),
    present(1),
    absent(2) // absent in this context means the user is requesting absence
}