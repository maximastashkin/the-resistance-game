package ru.tinkoff.resistance.service.game

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.tinkoff.resistance.game.Game
import java.time.LocalDateTime

class GameDao(private val db: Database) {
    fun create(hostId: Int, hostName: String, dateTime: LocalDateTime, winner: Int): Game = transaction(db) {
        val id = Games.insertAndGetId {
            it[Games.hostId] = hostId
            it[Games.dateTime] = dateTime
            it[Games.winner] = winner
        }
        Game(id.value, hostId, hostName)
    }

    fun update(id: Int, winner: Int) {
        Games.update({ Games.id eq id }) {
            it[Games.winner] = winner
        }
    }
}