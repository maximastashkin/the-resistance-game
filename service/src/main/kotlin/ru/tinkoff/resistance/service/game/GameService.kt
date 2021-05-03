package ru.tinkoff.resistance.service.game

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.tinkoff.resistance.game.Game

class GameService(
    private val dao: GameDao,
    private val db: Database
) {
    fun create(hostId: Int, hostName: String): Game = transaction(db) {
        dao.create(hostId, hostName)
    }
}