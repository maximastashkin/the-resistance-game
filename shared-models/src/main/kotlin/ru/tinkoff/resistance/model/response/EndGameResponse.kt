package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.role.Role

@Serializable
data class EndGameResponse(
    val basicInfoResponse: BasicInfoResponse,
    val winner: Role
)
