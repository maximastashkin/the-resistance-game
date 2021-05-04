package ru.tinkoff.resistance.requests.models.game

import kotlinx.serialization.Serializable

@Serializable
data class JoinGameRequest(val apiId: Long, val gameId: Int)
