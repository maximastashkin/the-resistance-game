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

    fun executeCommandWithGameStateChangeHandle(
        gameId: Int,
        gameService: GameService,
        playerService: PlayerService,
        command: Command
    ) {
        val game = getGameById(gameId)
        game.onGameStateChanged = endGameStatementHandler(game, gameService, playerService)
        game.executeCommand(command)
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

    private fun endGameStatementHandler(
        game: Game,
        gameService: GameService,
        playerService: PlayerService,
    ) = { _: GameState, new: GameState ->
        if (new == GameState.END) {
            gameService.update(game.id, game.winner.num)
            kickPlayersFromGame(game, playerService)
            deleteGameFromActive(game)
        }
    }

    private fun deleteGameFromActive(game: Game) = activeGames.remove(game)
}