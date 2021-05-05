package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable

@Serializable
data class TeamingInfoResponse(
    val basicInfoResponse: BasicInfoResponse,
    val missionLeaderApiId: Long,
)
