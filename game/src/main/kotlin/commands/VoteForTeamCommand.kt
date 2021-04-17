package commands

import Game

class VoteForTeamCommand(
    override val senderId: Int,
    override val senderName: String,
    private val agreement: Boolean
) : Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.VOTING) {
            throw CommandExecutionException("Попытка голосования за команду во время другой стадии игры ($senderId, $senderName).")
        }

        if (game.isVoteToTeam(senderId)) {
            throw CommandExecutionException("Игрок уже проголосовал за команду ($senderId, $senderName).")
        }

        game.setVote(senderId, agreement)
    }
}