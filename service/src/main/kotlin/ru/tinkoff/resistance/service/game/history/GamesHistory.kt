package ru.tinkoff.resistance.service.game.history

import org.jetbrains.exposed.sql.Table

object GamesHistory : Table() {
    var playerId = integer("player_id")
    var gameId = integer("game_id")
}