import org.junit.jupiter.api.Test
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameState
import ru.tinkoff.resistance.game.commands.JoinGameCommand
import ru.tinkoff.resistance.game.commands.StartGameCommand

class TestGameStateEvent {

    @Test
    fun `test method execution when game state has been changed`() {
        val game = Game(100001, 1, "Павел")
        var isChanged = false
        game.onGameStateChanged = { _, _ ->
            isChanged = true
        }
        assert(game.playerCount() == 1)
        assert(game.gameState == GameState.LOBBY)
        game.executeCommand(JoinGameCommand(2, "Максим"))
        game.executeCommand(JoinGameCommand(3, "Артем"))
        game.executeCommand(JoinGameCommand(4, "Алексей"))
        game.executeCommand(JoinGameCommand(5, "Юрий"))
        game.executeCommand(StartGameCommand(1, "Павел"))
        assert(game.gameState == GameState.TEAMING)
        assert(isChanged)
    }

}