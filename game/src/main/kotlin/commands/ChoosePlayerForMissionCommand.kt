package commands

import Game
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
            throw CommandExecutionException("Попытка добавления игрока во время другой стадии игры ($senderId, $senderName, $userToMission).")
        }

        if (senderId != game.getLeaderId()) {
            throw CommandExecutionException("Попытка добавления игрока в команду не лидером ($senderId, $senderName).")
        }

        if (game.isContainsTeammate(userToMission)) {
            throw CommandExecutionException("Попытка добавления игрока, который уже в команде ($senderId, $senderName, $userToMission).")
        }

        game.addPlayerToMission(userToMission)
    }
}