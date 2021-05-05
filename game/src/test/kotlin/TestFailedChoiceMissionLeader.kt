import ru.tinkoff.resistance.game.commands.ChoosePlayerForMissionCommand
import ru.tinkoff.resistance.game.commands.JoinGameCommand
import ru.tinkoff.resistance.game.commands.StartGameCommand
import ru.tinkoff.resistance.game.commands.VoteForTeamCommand
import org.junit.jupiter.api.Test
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameState
import ru.tinkoff.resistance.model.role.Role

class TestFailedChoiceMissionLeader {

    @Test
    fun `test 5 fails of choosing of mission leader`() {
        val game = Game(100001, 1, "Павел")

        assert(game.playerCount() == 1)
        assert(game.gameState == GameState.LOBBY)
        game.executeCommand(JoinGameCommand(2, "Максим"))
        game.executeCommand(JoinGameCommand(3, "Артем"))
        game.executeCommand(JoinGameCommand(4, "Алексей"))
        game.executeCommand(JoinGameCommand(5, "Юрий"))
        game.executeCommand(JoinGameCommand(6, "Владимир"))
        game.executeCommand(JoinGameCommand(7, "Александр"))
        game.executeCommand(JoinGameCommand(8, "Сергей"))
        game.executeCommand(JoinGameCommand(9, "Иван"))
        game.executeCommand(JoinGameCommand(10, "Виталий"))

        assert(game.playerCount() == 10)

        game.executeCommand(StartGameCommand(1, "Павел"))
        assert(game.gameState == GameState.TEAMING)

        repeat(5) {
            var playersToMission = game.getPlayersToMission()
            var playersExcludeLeader = game.getPlayersExcludeLeader()
            var leader = game.getLeader()

            playersToMission.forEach {
                game.executeCommand(ChoosePlayerForMissionCommand(leader.id, leader.name, it.id))
            }
            assert(game.gameState == GameState.VOTING)

            game.executeCommand(VoteForTeamCommand(leader.id, leader.name, true))
            playersExcludeLeader.forEach { player ->
                game.executeCommand(VoteForTeamCommand(player.id, player.name, false))
            }
            assert(game.failedVotes == it + 1)
        }
        assert(game.gameState == GameState.END)
        assert(game.winner == Role.TRAITOR)
    }

}