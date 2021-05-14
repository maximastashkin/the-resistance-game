package ru.tinkoff.resistance.model.request

import kotlinx.serialization.Serializable

@Serializable
data class VoteForTeamRequest(val apiId: Long, val agreement: Boolean)
