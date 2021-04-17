package commands

import Game

class MissionActionCommand(
    override val senderId: Int,
    override val senderName: String,
    private val action: Boolean
) : Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.MISSION) {
            throw CommandExecutionException("Попытка голосования в миссии в другой стадии игры ($senderId, $senderName).")
        }

        if (!game.isInTeam(senderId)) {
            throw CommandExecutionException("Игрок не учавствует в миссии ($senderId, $senderName).")
        }

        if (game.doneMission(senderId)) {
            throw CommandExecutionException("Игрок уже сделал миссию ($senderId, $senderName).")
        }

        game.setMissionVote(senderId, action)
    }
}