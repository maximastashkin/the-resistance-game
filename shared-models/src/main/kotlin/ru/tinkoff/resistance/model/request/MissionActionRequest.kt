package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class MissionActionRequest(val apiId: Long, val action: Boolean)
