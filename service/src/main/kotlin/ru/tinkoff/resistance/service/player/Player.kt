package ru.tinkoff.resistance.service.player

import kotlinx.serialization.Serializable

@Serializable
class Player(val id: Int, val apiId: Long, val name: String, val currentGameId: Int?) {
    fun isActive() = currentGameId != null
}
