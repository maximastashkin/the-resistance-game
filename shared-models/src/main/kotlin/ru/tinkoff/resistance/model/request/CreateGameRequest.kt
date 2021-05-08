package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateGameRequest(val hostApiId: Long)