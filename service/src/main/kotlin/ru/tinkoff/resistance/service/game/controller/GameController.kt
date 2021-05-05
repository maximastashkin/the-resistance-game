package ru.tinkoff.resistance.service.game.controller

import ru.tinkoff.resistance.errocodes.GameErrorCode
import ru.tinkoff.resistance.game.Game

class GameController {
    private val activeGames = mutableListOf<Game>()

    fun addGameToActive(game: Game) = activeGames.add(game)

    fun getGameById(gameId: Int): Game {
        if (gameId == -1) {
            throw GameNotFoundException("Player not in game", GameErrorCode.PLAYER_NOT_IN_GAME)
        }
        return activeGames.find {
            it.id == gameId
        } ?: throw GameNotFoundException("Game with id = $gameId not found.", GameErrorCode.GAME_NOT_FOUND)
    }
}