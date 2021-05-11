package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game

class EarlyFinishGameCommand(override val senderId: Int, override val senderName: String) : Command {
    override fun execute(game: Game) {
        if (game.getPlayerById(senderId) == null) {
            throw CommandExecutionException(
                "Игрок ($senderId, $senderName) не в игре.",
                CommandErrorCode.PLAYER_NOT_IN_GAME
            )
        }
        game.earlyFinishGame()
    }
}