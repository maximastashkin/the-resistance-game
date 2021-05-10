package ru.tinkoff.resistance.service.game

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.response.*
import ru.tinkoff.resistance.service.player.PlayerService

class GameResponsesFormer {
    companion object {
        fun formInfoResponse(game: Game, service: PlayerService) = InfoResponse(
            gameState = game.gameState,
            notTraitors = getPlayersApiIds(game.getNotTraitors(), service),
            traitors = getPlayersApiIds(game.getTraitors(), service),
            teammates = getPlayersApiIds(game.teammates.keys.map { game.getPlayerById(it)!! }, service),
            countFailedMissions = game.getCountFailedMissions(),
            countSuccessedMissions = game.getCountSuccessedMissions(),
            missionLeader = getPlayerApiIdAndName(game.getLeader(), service)
        )

        private fun getPlayersApiIds(
            players: List<ru.tinkoff.resistance.game.Player>,
            service: PlayerService
        ): List<Pair<Long, String>> {
            val output = mutableListOf<Pair<Long, String>>()
            players.forEach {
                output.add(getPlayerApiIdAndName(it, service))
            }
            return output
        }

        private fun getPlayerApiIdAndName(
            player: ru.tinkoff.resistance.game.Player,
            service: PlayerService
        ): Pair<Long, String> {
            val gamePlayer = service.findById(player.id)
            return Pair(gamePlayer.apiId, gamePlayer.name)
        }
    }
}