package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.game.GameState

@Serializable
data class InfoResponse(
    val gameState: GameState,
    val notTraitors: List<Pair<Long, String>>,
    val traitors: List<Pair<Long, String>>,
    val teammates: List<Pair<Long, String>>,
    val countFailedMissions: Int, val countSuccessedMissions: Int,
    val missionLeader: Pair<Long, String>,
)