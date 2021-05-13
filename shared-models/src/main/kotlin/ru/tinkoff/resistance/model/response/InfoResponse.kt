package ru.tinkoff.resistance.model.response

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.model.game.MissionResult
import ru.tinkoff.resistance.model.game.Role

@Serializable
data class InfoResponse(
    val gameState: GameState,
    val notTraitors: List<Pair<Long, String>>,
    val traitors: List<Pair<Long, String>>,
    val teammates: List<Pair<Long, String>>,
    val countFailedMissions: Int, val countSuccessedMissions: Int,
    val missionLeader: Pair<Long, String>,
    val lastMissionResult: MissionResult,
    val lastMissionVotes: Pair<Int, Int>,
    val winner: Role,
)