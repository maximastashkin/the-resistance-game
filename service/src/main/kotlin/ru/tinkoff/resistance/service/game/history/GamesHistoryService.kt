package ru.tinkoff.resistance.service.game.history

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class GamesHistoryService(
    private val db: Database,
    private val dao: GamesHistoryDao
) {
    fun create(playerId: Int, gameId: Int) = transaction(db) {
        dao.create(playerId, gameId)
    }
}