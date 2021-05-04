package ru.tinkoff.resistance.service.player

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.IntegerColumnType

object Players : IntIdTable() {
    var apiId = long("api_id")
    var name = varchar("name", 50)
    var currentGameId = integer("current_game_id").nullable()
}