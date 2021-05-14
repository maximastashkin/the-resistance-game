package ru.tinkoff.resistance.game

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.game.MissionResult

@Serializable
class Mission(val players: Int, var missionResult: MissionResult)