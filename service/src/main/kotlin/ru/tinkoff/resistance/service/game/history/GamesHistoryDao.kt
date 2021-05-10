package ru.tinkoff.resistance.service.game.history

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class GamesHistoryDao(private val db: Database) {
    fun create(playerId: Int, gameId: Int) = transaction(db) {
        GamesHistory.insert {
            it[GamesHistory.playerId] = playerId
            it[GamesHistory.gameId] = gameId
        }
    }
}