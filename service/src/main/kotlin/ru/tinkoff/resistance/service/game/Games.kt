package ru.tinkoff.resistance.service.game

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object Games : IntIdTable() {
    var hostId = integer("host_id")
    var dateTime = datetime("date_time")
    var winner = integer("winner")
}