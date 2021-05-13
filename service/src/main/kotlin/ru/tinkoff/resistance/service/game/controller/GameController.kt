package ru.tinkoff.resistance.service.game.controller

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.commands.Command
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.service.game.InfoResponseFormer
import ru.tinkoff.resistance.service.game.GameService
import ru.tinkoff.resistance.service.game.history.GamesHistoryService
import ru.tinkoff.resistance.service.player.PlayerService

class GameController {
    private val activeGames = mutableListOf<Game>()

    fun addGameToActive(game: Game) = activeGames.add(game)

    fun getGameById(gameId: Int): Game {
        if (gameId == -1) {
            throw GameNotFoundException("Player not in game", CommandErrorCode.PLAYER_NOT_IN_GAME)
        }
        return activeGames.find {
            it.id == gameId
        } ?: throw GameNotFoundException("Game with id = $gameId not found.", CommandErrorCode.GAME_NOT_FOUND)
    }

    fun getAllActivePlayersApiIds(playerService: PlayerService): List<Long> = activeGames.map {
        it.players.map { player ->
            playerService.findById(player.id).apiId
        }
    }.flatten()

    fun executeCommand(
        gameId: Int,
        command: Command
    ) {
        val game = getGameById(gameId)
        game.executeCommand(command)
    }

    fun closeGame(
        gameId: Int,
        gameService: GameService,
        playerService: PlayerService
    ) {
        val game = getGameById(gameId)
        gameService.update(game.id, game.winner.num)
        kickPlayersFromGame(game, playerService)
        deleteGameFromActive(game)
    }

    fun getInfoResponse(gameId: Int, infoResponseFormer: InfoResponseFormer) =
        infoResponseFormer.formInfoResponsePair(getGameById(gameId))

    fun createGameHistory(
        gameId: Int,
        service: GamesHistoryService
    ) {
        getGameById(gameId).players.map {
            it.id
        }.forEach {
            service.create(it, gameId)
        }
    }

    private fun kickPlayersFromGame(game: Game, playerService: PlayerService) {
        game.players.forEach {
            val currentEntity = playerService.findById(it.id)
            playerService.update(currentEntity.id, currentEntity.apiId, currentEntity.name, -1)
        }
    }

    private fun deleteGameFromActive(game: Game) = activeGames.remove(game)
}