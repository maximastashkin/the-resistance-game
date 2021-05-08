package ru.tinkoff.resistance.service.game

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton
import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.commands.*
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.model.request.*
import ru.tinkoff.resistance.service.game.controller.GameController
import ru.tinkoff.resistance.service.game.history.GamesHistoryDao
import ru.tinkoff.resistance.service.game.history.GamesHistoryService
import ru.tinkoff.resistance.service.player.PlayerService
import java.time.LocalDateTime

fun Application.gameModule() {
    val service: GameService by closestDI().instance()
    val controller: GameController by closestDI().instance()
    val playerService: PlayerService by closestDI().instance()
    val gamesHistoryService: GamesHistoryService by closestDI().instance()
    routing {
        route("/game/create") {
            post {
                val apiId = call.receive<CreateGameRequest>().hostApiId
                with(playerService.findByApiId(apiId)) {
                    if (!isActive()) {
                        val createdGame = service.create(id, name, LocalDateTime.now(), 0)
                        controller.addGameToActive(createdGame)
                        playerService.update(id, apiId, name, createdGame.id)
                        call.respond(HttpStatusCode.Created, createdGame.id)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, CommandErrorCode.ALREADY_IN_GAME)
                    }
                }
            }
        }
        route("/game/join") {
            post {
                val request = call.receive<JoinGameRequest>()
                with(playerService.findByApiId(request.apiId)) {
                    controller.getGameById(request.gameId).executeCommand(JoinGameCommand(id, name))
                    playerService.update(id, apiId, name, request.gameId)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
        route("/game/start/{hostApiId}") {
            get {
                val apiId = call.parameters["hostApiId"]?.toLong()
                if (apiId != null) {
                    with(playerService.findByApiId(apiId)) {
                        val game = controller.getGameById(currentGameId ?: -1)
                        respondFormingResponse(game, playerService, StartGameCommand(id, name))
                        game.players.map {
                            it.id
                        }.forEach {
                            gamesHistoryService.create(it, game.id)
                        }
                    }
                } else {
                    // Плохой запрос
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/game/chooseplayerformission") {
            post {
                val request = call.receive<ChoosePlayerForMissionRequest>()
                with(playerService.findByApiId(request.leaderApiId)) {
                    val candidate = playerService.findByApiId(request.candidateApiId)
                    val game = controller.getGameById(currentGameId ?: -1)
                    respondFormingResponse(game, playerService, ChoosePlayerForMissionCommand(id, name, candidate.id))
                }
            }
        }
        route("/game/voteforteam") {
            post {
                val request = call.receive<VoteForTeamRequest>()
                with(playerService.findByApiId(request.apiId)) {
                    val game = controller.getGameById(currentGameId ?: -1)
                    game.onGameStateChanged = {_, new ->
                        if (new == GameState.END) {
                            service.update(game.id, game.winner.ordinal)
                        }
                    }
                    respondFormingResponse(game, playerService, VoteForTeamCommand(id, name, request.agreement))
                }
            }
        }
        route("/game/missionaction") {
            post {
                val request = call.receive<MissionActionRequest>()
                with(playerService.findByApiId(request.apiId)) {
                    val game = controller.getGameById(currentGameId ?: -1)
                    game.onGameStateChanged = {_, new ->
                        if (new == GameState.END) {
                            service.update(game.id, game.winner.ordinal)
                        }
                    }
                    respondFormingResponse(game, playerService, MissionActionCommand(id, name, request.action))
                }
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respondFormingResponse(
    game: Game,
    playerService: PlayerService,
    command: Command
) {
    game.executeCommand(command)
    call.respond(HttpStatusCode.OK, GameResponsesFormer.formInfoResponse(game, playerService))
}

fun DI.Builder.gameComponents() {
    bind<GameDao>() with singleton { GameDao(instance()) }
    bind<GameService>() with singleton { GameService(instance(), instance()) }
    bind<GameController>() with singleton { GameController() }
    bind<GamesHistoryDao>() with singleton { GamesHistoryDao(instance()) }
    bind<GamesHistoryService>() with singleton { GamesHistoryService(instance(), instance()) }
}