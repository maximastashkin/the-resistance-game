package ru.tinkoff.resistance.service.game.controller

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.commands.JoinGameCommand
import ru.tinkoff.resistance.service.player.Player
import java.lang.Exception

class GameController {
    private val activeGames = mutableListOf<Game>()

    fun addGameToActive(game: Game) = activeGames.add(game)

    fun getGameById(gameId: Int): Game {
        return activeGames.find {
            it.id == gameId
        } ?: throw Exception()
    }
}