package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameConfiguration
import ru.tinkoff.resistance.model.game.GameState

/**
 * Команда для подключения к сессии
 */
class JoinGameCommand(override val senderId: Int,override val senderName: String) : Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.LOBBY) {
            throw CommandExecutionException("Попытка входа в запущенную игру от игрока ($senderId, $senderName).", CommandErrorCode.ENTER_TO_STARTED_GAME)
        }
        if (game.getPlayerById(senderId) != null) {
            throw CommandExecutionException("Игрок ($senderId, $senderName) уже в игре.", CommandErrorCode.ALREADY_IN_GAME)
        }
        if (game.playerCount() == GameConfiguration.MAX_PLAYERS) {
            throw CommandExecutionException("В игре уже максимальное количество игроков ($senderId, $senderName).", CommandErrorCode.FULL_LOBBY)
        }

        game.addPlayer(senderId,senderName)
    }
}