import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameConfiguration
import ru.tinkoff.resistance.game.GameState
import ru.tinkoff.resistance.model.role.Role
import ru.tinkoff.resistance.game.commands.*

class TestWholeGame {

    @Test
    fun `whole game 4 rounds and traitor's win`() {
        val game = Game(100001, 1, "Павел")

        assert(game.playerCount() == 1)
        assert(game.gameState == GameState.LOBBY)

        game.executeCommand(JoinGameCommand(2, "Максим"))
        assertThrows<CommandExecutionException> { game.executeCommand(JoinGameCommand(2, "Максим")) }.also { println(it.message) }
        assertThrows<CommandExecutionException> { game.executeCommand(StartGameCommand(2, "Максим")) }.also { println(it.message) }
        assertThrows<CommandExecutionException> { game.executeCommand(StartGameCommand(1, "Павел")) }.also { println(it.message) }

        game.executeCommand(JoinGameCommand(3, "Артем"))
        game.executeCommand(JoinGameCommand(4, "Алексей"))
        game.executeCommand(JoinGameCommand(5, "Юрий"))
        game.executeCommand(JoinGameCommand(6, "Владимир"))
        game.executeCommand(JoinGameCommand(7, "Александр"))
        game.executeCommand(JoinGameCommand(8, "Сергей"))
        game.executeCommand(JoinGameCommand(9, "Иван"))
        game.executeCommand(JoinGameCommand(10, "Виталий"))

        assert(game.playerCount() == 10)
        assertThrows<CommandExecutionException> { game.executeCommand(JoinGameCommand(11, "Илья")) }.also { println(it.message) }

        assertThrows<CommandExecutionException> { game.executeCommand(ChoosePlayerForMissionCommand(11, "Илья", 1)) }.also { println(it.message) }
        assertThrows<CommandExecutionException> { game.executeCommand(MissionActionCommand(11, "Илья", true)) }.also { println(it.message) }
        assertThrows<CommandExecutionException> { game.executeCommand(VoteForTeamCommand(11, "Илья", false)) }.also { println(it.message) }

        assertThrows<CommandExecutionException> { game.executeCommand(StartGameCommand(2, "Максим")) }.also { println(it.message) }

        game.executeCommand(StartGameCommand(1, "Павел"))
        assert(game.gameState == GameState.TEAMING)

        val traitors = game.getTraitors()
        var missionLeader = game.missionLeader
        var leader = game.getLeader()

        var playersToMission = game.getPlayersToMission()

        var playersExcludeLeader = game.getPlayersExcludeLeader()

        assert(traitors.size == GameConfiguration.getRoles(game.playerCount())!!.second)
        assert(GameConfiguration.getMissions(game.playerCount())!![game.currentMission] == 3)

        assertThrows<CommandExecutionException> { game.executeCommand(ChoosePlayerForMissionCommand(leader.id, leader.name, leader.id)) }.also { println(it.message) }
        assertThrows<CommandExecutionException> { game.executeCommand(ChoosePlayerForMissionCommand(playersToMission[0].id, playersToMission[0].name, leader.id)) }.also { println(it.message) }

        playersToMission.forEach {
            game.executeCommand(ChoosePlayerForMissionCommand(leader.id, leader.name, it.id))
        }

        assert(game.gameState == GameState.VOTING)

        game.executeCommand(VoteForTeamCommand(leader.id, leader.name, true))
        assertThrows<CommandExecutionException> { game.executeCommand(VoteForTeamCommand(leader.id, leader.name, false)) }.also { println(it.message) }

        playersExcludeLeader.forEach {
            game.executeCommand(VoteForTeamCommand(it.id, it.name, false))
        }

        assert(game.gameState == GameState.TEAMING)
        assert(missionLeader != game.missionLeader)

        missionLeader = game.missionLeader
        leader = game.getLeader()
        playersToMission = game.getPlayersToMission()
        playersExcludeLeader = game.getPlayersExcludeLeader()

        playersToMission.forEach {
            game.executeCommand(ChoosePlayerForMissionCommand(leader.id, leader.name, it.id))
        }

        assert(game.gameState == GameState.VOTING)
        game.executeCommand(VoteForTeamCommand(leader.id, leader.name, true))
        playersExcludeLeader.forEach {
            game.executeCommand(VoteForTeamCommand(it.id, it.name, true))
        }

        assert(game.gameState == GameState.MISSION)
        val lastPlayer = game.getLastPlayer()

        assertThrows<CommandExecutionException> { game.executeCommand(MissionActionCommand(lastPlayer.id, lastPlayer.name, false)) }.also { println(it.message) }

        game.executeCommand(MissionActionCommand(leader.id, leader.name, true))
        assertThrows<CommandExecutionException> { game.executeCommand(MissionActionCommand(leader.id, leader.name, true)) }.also { println(it.message) }

        playersToMission.forEach {
            game.executeCommand(MissionActionCommand(it.id, it.name, true))
        }

        assert(game.gameState == GameState.TEAMING)
        assert(game.getCountSuccessedMissions() == 1)
        assert(game.currentMission == 1)

        repeat(3) {
            missionLeader = game.missionLeader
            leader = game.getLeader()
            playersToMission = game.getPlayersToMission()
            playersExcludeLeader = game.getPlayersExcludeLeader()

            playersToMission.forEach {
                game.executeCommand(ChoosePlayerForMissionCommand(leader.id, leader.name, it.id))
            }
            assert(game.gameState == GameState.VOTING)
            game.executeCommand(VoteForTeamCommand(leader.id, leader.name, true))
            playersExcludeLeader.forEach {
                game.executeCommand(VoteForTeamCommand(it.id, it.name, true))
            }
            assert(game.gameState == GameState.MISSION)

            game.executeCommand(MissionActionCommand(leader.id, leader.name, false))
            playersToMission.forEach {
                game.executeCommand(MissionActionCommand(it.id, it.name, true))
            }

            assert(game.getCountFailedMissions() == (it + 1))
        }

        assert(game.currentMission == 3)
        assert(game.gameState == GameState.END)
        assert(game.winner == Role.TRAITOR)
    }
}