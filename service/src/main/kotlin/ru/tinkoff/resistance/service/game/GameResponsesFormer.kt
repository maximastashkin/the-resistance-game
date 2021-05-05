package ru.tinkoff.resistance.service.game

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.response.BasicInfoResponse
import ru.tinkoff.resistance.model.response.EndGameResponse
import ru.tinkoff.resistance.model.response.TeamingInfoResponse
import ru.tinkoff.resistance.model.response.VotingInfoResponse
import ru.tinkoff.resistance.service.player.PlayerService

class GameResponsesFormer {
    companion object {
        fun formEndGameResponse(game: Game, service: PlayerService) = EndGameResponse(
            formBasicInfoResponse(game, service),
            game.winner
        )

        fun formTeamingInfoResponse(game: Game, service: PlayerService) = TeamingInfoResponse(
            formBasicInfoResponse(game, service),
            getPlayerApiId(game.getLeader(), service)
        )

        fun formVotingInfoResponse(game: Game, service: PlayerService) = VotingInfoResponse(
            getPlayersApiIds(game.players, service),
            getPlayersApiIds(
                game.teammates.keys.map { game.getPlayerById(it)!! }, service
            )
        )

        fun formBasicInfoResponse(game: Game, service: PlayerService) = BasicInfoResponse(
            getPlayersApiIds(game.getNotTraitors(), service),
            getPlayersApiIds(game.getTraitors(), service),
            game.getCountFailedMissions(),
            game.getCountSuccessedMissions()
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