package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.model.game.GameState

/**
 * Команда для голосования за выбор команды для миссии
 * @property agreement результат голоса за выбор команды для миссии (true - agree, false - disagree)
 */
class VoteForTeamCommand(
    override val senderId: Int,
    override val senderName: String,
    private val agreement: Boolean
) : Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.VOTING) {
            throw CommandExecutionException(
                "Попытка голосования за команду во время другой стадии игры ($senderId, $senderName).",
                CommandErrorCode.VOTE_IN_NOT_VOTE_STATE
            )
        }

        if (game.isVoteToTeam(senderId)) {
            throw CommandExecutionException(
                "Игрок уже проголосовал за команду ($senderId, $senderName).",
                CommandErrorCode.ALREADY_VOTE
            )
        }

        game.setTeamVote(senderId, agreement)
    }
}