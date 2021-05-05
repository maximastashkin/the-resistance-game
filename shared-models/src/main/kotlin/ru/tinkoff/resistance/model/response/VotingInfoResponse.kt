package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable

@Serializable
data class VotingInfoResponse(val playersApiIds: List<Long>, val teammatesApiIds: List<Long>)
