package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ChoosePlayerForMissionRequest(val leaderApiId: Long, val candidateApiId: Long)