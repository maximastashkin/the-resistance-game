package ru.tinkoff.resistance.requests.models.player

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCreateRequest(val apiId: Long, val name: String)