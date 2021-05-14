package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PlayerCreateRequest(val apiId: Long, val name: String)