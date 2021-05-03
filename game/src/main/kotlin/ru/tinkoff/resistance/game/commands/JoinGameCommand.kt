package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameConfiguration
import ru.tinkoff.resistance.game.GameState

/**
 * Команда для подключения к сессии
 */
class JoinGameCommand(override val senderId: Int,override val senderName: String) : Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.LOBBY) {
            throw CommandExecutionException("Попытка входа в запущенную игру от игрока ($senderId, $senderName).")
        }
        if (game.getPlayerById(senderId) != null) {
            throw CommandExecutionException("Игрок ($senderId, $senderName) уже в игре.")
        }
        if (game.playerCount() == GameConfiguration.MAX_PLAYERS) {
            throw CommandExecutionException("В игре уже максимальное количество игроков ($senderId, $senderName).")
        }

        game.addPlayer(senderId,senderName)
    }
}