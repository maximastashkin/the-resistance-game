package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class JoinGameRequest(val apiId: Long, val gameId: Int)
