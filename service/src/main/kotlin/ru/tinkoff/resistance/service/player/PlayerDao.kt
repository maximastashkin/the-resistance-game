package ru.tinkoff.resistance.service.player

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerDao(private val db: Database) {

    fun findAll(): List<Player> = transaction(db) {
        Players.selectAll().map(::extractPlayer)
    }

    fun findById(id: Int): Player = transaction(db) {
        extractPlayer(Players.select {
            Players.id eq id
        }.first())
    }

    fun findByApiId(apiId: Long): Player = transaction(db) {
        extractPlayer(Players.select {
            Players.apiId eq apiId
        }.first())
    }

    fun create(apiId: Long, name: String, currentGameId: Int): Player = transaction(db) {
        runCatching {
            val id = Players.insertAndGetId {
                it[Players.apiId] = apiId
                it[Players.name] = name
                it[Players.currentGameId] = currentGameId
            }
            Player(id.value, apiId, name, currentGameId)
        }.getOrElse {
            Player(-1, -1, "", -1)
        }
    }

    fun update(id: Int, apiId: Long, name: String, currentGameId: Int): Int = transaction(db) {
        Players.update({ Players.id eq id }) {
            it[Players.apiId] = apiId
            it[Players.name] = name
            it[Players.currentGameId] = currentGameId
        }
    }

    fun delete(id: Int): Int = transaction(db) {
        Players.deleteWhere {
            Players.id eq id
        }
    }

    private fun extractPlayer(row: ResultRow): Player = Player(
        row[Players.id].value,
        row[Players.apiId],
        row[Players.name],
        row[Players.currentGameId]
    )
}