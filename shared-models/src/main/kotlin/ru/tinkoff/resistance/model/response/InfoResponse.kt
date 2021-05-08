package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.game.GameState

@Serializable
data class InfoResponse(
    val gameState: GameState,
    val notTraitorsApiIds: List<Long>,
    val traitorsApiIds: List<Long>,
    val teammatesApiIds: List<Long>,
    val countFailedMissions: Int, val countSuccessedMissions: Int,
    val missionLeaderApiId: Long,
)