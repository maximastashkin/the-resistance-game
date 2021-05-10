package ru.tinkoff.resistance.service.game

import io.ktor.http.*
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.response.*
import ru.tinkoff.resistance.service.player.PlayerService

class InfoResponseFormer(private val service: PlayerService) {
    fun formInfoResponsePair(game: Game) = Pair(
        HttpStatusCode.OK, InfoResponse(
            gameState = game.gameState,
            notTraitors = getPlayersApiIds(game.getNotTraitors()),
            traitors = getPlayersApiIds(game.getTraitors()),
            teammates = getPlayersApiIds(game.teammates.keys.map { game.getPlayerById(it)!! }),
            countFailedMissions = game.getCountFailedMissions(),
            countSuccessedMissions = game.getCountSuccessedMissions(),
            missionLeader = getPlayerApiIdAndName(game.getLeader()),
            lastMissionResult = game.getLastMissionResult(),
            winner = game.winner
        )
    )

    private fun getPlayersApiIds(
        players: List<ru.tinkoff.resistance.game.Player>,
    ): List<Pair<Long, String>> {
        val output = mutableListOf<Pair<Long, String>>()
        players.forEach {
            output.add(getPlayerApiIdAndName(it))
        }
        return output
    }

    private fun getPlayerApiIdAndName(
        player: ru.tinkoff.resistance.game.Player,
    ): Pair<Long, String> {
        val gamePlayer = service.findById(player.id)
        return Pair(gamePlayer.apiId, gamePlayer.name)
    }
}