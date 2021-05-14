import org.junit.jupiter.api.Test
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.commands.JoinGameCommand
import ru.tinkoff.resistance.game.commands.StartGameCommand
import ru.tinkoff.resistance.model.game.GameState

class TestGameForShufflePlayers {

    @Test
    fun `test shuffle same players in different lobby`() {
        val game1 = Game(100001, 1, "Павел")
        assert(game1.playerCount() == 1)
        assert(game1.gameState == GameState.LOBBY)
        game1.executeCommand(JoinGameCommand(2, "Максим"))
        game1.executeCommand(JoinGameCommand(3, "Артем"))
        game1.executeCommand(JoinGameCommand(4, "Алексей"))
        game1.executeCommand(JoinGameCommand(5, "Юрий"))
        game1.executeCommand(StartGameCommand(1, "Павел"))
        assert(game1.gameState == GameState.TEAMING)

        val game2 = Game(100002, 1, "Павел")
        assert(game2.playerCount() == 1)
        assert(game2.gameState == GameState.LOBBY)
        game2.executeCommand(JoinGameCommand(2, "Максим"))
        game2.executeCommand(JoinGameCommand(3, "Артем"))
        game2.executeCommand(JoinGameCommand(4, "Алексей"))
        game2.executeCommand(JoinGameCommand(5, "Юрий"))
        game2.executeCommand(StartGameCommand(1, "Павел"))
        assert(game2.gameState == GameState.TEAMING)

        assert(game1.getTraitors() != game2.getTraitors())
    }

}