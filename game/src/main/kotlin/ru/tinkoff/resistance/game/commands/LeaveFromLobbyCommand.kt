package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.game.GameState

class LeaveFromLobbyCommand(override val senderId: Int, override val senderName: String) : Command {
    override fun execute(game: Game) {
        if (game.getPlayerById(senderId) == null) {
            throw CommandExecutionException(
                "Игрок ($senderId, $senderName) не в игре.",
                CommandErrorCode.PLAYER_NOT_IN_GAME
            )
        }
        if (game.gameState != GameState.LOBBY) {
            throw CommandExecutionException(
                "Попытка выхода из запущенной игры ($senderId, $senderName).",
                CommandErrorCode.LEAVE_FROM_LOBBY_IN_NOT_LOBBY_STATE
            )
        }


    }
}