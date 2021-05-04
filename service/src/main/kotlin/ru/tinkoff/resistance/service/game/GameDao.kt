package ru.tinkoff.resistance.service.game

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import ru.tinkoff.resistance.game.Game

class GameDao(private val db: Database) {
    fun create(hostId: Int, hostName: String): Game = transaction(db) {
        val id = Games.insertAndGetId {}
        Game(id.value, hostId, hostName)
    }
}