package ru.tinkoff.resistance.service.player

import io.ktor.features.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerService(
    private val dao: PlayerDao,
    private val db: Database
) {
    fun findAll(): List<Player> = dao.findAll()

    fun findById(id: Int): Player = dao.findById(id)

    fun findByApiId(apiId: Long): Player {
        runCatching {
            dao.findByApiId(apiId)
        }.onSuccess {
            return it
        }.onFailure {
            throw PlayerNotFoundException(apiId)
        }
        return Player(-1, -1, "", null)
    }

    fun create(apiId: Long, name: String, currentGameId: Int?): Player = transaction(db) {
        dao.create(apiId, name, currentGameId)
    }

    fun update(id: Int, telegramId: Long, name: String, currentGameId: Int?): Int = transaction(db) {
        dao.update(id, telegramId, name, currentGameId)
    }

    fun delete(id: Int) = transaction(db) {
        dao.delete(id)
    }
}