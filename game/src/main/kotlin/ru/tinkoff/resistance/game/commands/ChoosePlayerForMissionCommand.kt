package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameState

/**
 * Команда для выбора лидером игрока для миссии
 * @property userToMission идентификатор выбранного игрока для миссии
 */
class ChoosePlayerForMissionCommand(
    override val senderId: Int,
    override val senderName: String,
    private val userToMission: Int
): Command {
    override fun execute(game: Game) {
        if (game.gameState != GameState.TEAMING) {
            throw CommandExecutionException("Попытка добавления игрока во время другой стадии игры ($senderId, $senderName, $userToMission).",
                CommandErrorCode.ADD_PLAYER_IN_NOT_TEAM_STATE)
        }

        if (senderId != game.getLeaderId()) {
            throw CommandExecutionException("Попытка добавления игрока в команду не лидером ($senderId, $senderName).", CommandErrorCode.NOT_LEADER_ADD_PLAYER_TO_TEAM)
        }

        if (game.isContainsTeammate(userToMission)) {
            throw CommandExecutionException("Попытка добавления игрока, который уже в команде ($senderId, $senderName, $userToMission).",
                CommandErrorCode.ALREADY_IN_TEAM)
        }

        game.addPlayerToMission(userToMission)
    }
}