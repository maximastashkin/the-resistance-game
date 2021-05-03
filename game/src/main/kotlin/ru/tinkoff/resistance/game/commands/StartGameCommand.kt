package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameConfiguration
import ru.tinkoff.resistance.game.GameState

/**
 * Команда для запуска игры
 */
class StartGameCommand(override val senderId: Int,override val senderName: String) : Command {
    override fun execute(game: Game) {
        if (!game.isHost(senderId)) {
            throw CommandExecutionException("Попытка запуска игры игроком ($senderId, $senderName).")
        }

        if (game.gameState != GameState.LOBBY) {
            throw CommandExecutionException("Попытка запуска игры уже запущенной игры ($senderId, $senderName).")
        }

        if (game.playerCount() < GameConfiguration.MIN_PLAYERS) {
            throw CommandExecutionException("Недостаточно игроков для запуска ($senderId, $senderName).")
        }

        game.startGame()
    }
}