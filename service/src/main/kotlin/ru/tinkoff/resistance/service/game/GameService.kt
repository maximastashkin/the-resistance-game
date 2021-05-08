package ru.tinkoff.resistance.service.game

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.tinkoff.resistance.game.Game
import java.time.LocalDateTime

class GameService(
    private val dao: GameDao,
    private val db: Database
) {
    fun create(hostId: Int, hostName: String, dateTime: LocalDateTime, winner: Int): Game = transaction(db) {
        dao.create(hostId, hostName, dateTime, winner)
    }

    fun update(id: Int, winner: Int) = transaction(db) {
        dao.update(id, winner)
    }
}