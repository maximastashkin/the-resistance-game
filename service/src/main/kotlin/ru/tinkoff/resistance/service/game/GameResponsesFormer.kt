package ru.tinkoff.resistance.service.game

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.response.*
import ru.tinkoff.resistance.service.player.PlayerService

class GameResponsesFormer {
    companion object {
        fun formInfoResponse(game: Game, service: PlayerService) = InfoResponse(
            gameState = game.gameState,
            notTraitorsApiIds = getPlayersApiIds(game.getNotTraitors(), service),
            traitorsApiIds = getPlayersApiIds(game.getTraitors(), service),
            teammatesApiIds = getPlayersApiIds(game.teammates.keys.map { game.getPlayerById(it)!! }, service),
            countFailedMissions = game.getCountFailedMissions(),
            countSuccessedMissions = game.getCountSuccessedMissions(),
            missionLeaderApiId = getPlayerApiId(game.getLeader(), service)
        )
        private fun getPlayersApiIds(players: List<ru.tinkoff.resistance.game.Player>, service: PlayerService): List<Long> {
            val output = mutableListOf<Long>()
            players.forEach {
                output.add(getPlayerApiId(it, service))
            }
            return output
        }

        private fun getPlayerApiId(player: ru.tinkoff.resistance.game.Player, service: PlayerService): Long =
            service.findById(player.id).apiId
    }
}