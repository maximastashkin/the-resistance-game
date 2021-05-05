package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BasicInfoResponse(
    val notTraitorsApiIds: List<Long>,
    val traitorsApiIds: List<Long>, val countFailedMissions: Int,
    val countSuccessedMissions: Int
)
