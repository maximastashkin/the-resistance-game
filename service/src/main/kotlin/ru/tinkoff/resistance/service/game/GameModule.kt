package ru.tinkoff.resistance.service.game

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton
import ru.tinkoff.resistance.game.Game
import ru.tinkoff.resistance.game.GameState
import ru.tinkoff.resistance.game.commands.*
import ru.tinkoff.resistance.model.request.ChoosePlayerForMissionRequest
import ru.tinkoff.resistance.model.request.JoinGameRequest
import ru.tinkoff.resistance.model.request.MissionActionRequest
import ru.tinkoff.resistance.model.request.VoteForTeamRequest
import ru.tinkoff.resistance.model.response.BasicInfoResponse
import ru.tinkoff.resistance.model.response.TeamingInfoResponse
import ru.tinkoff.resistance.service.game.controller.GameController
import ru.tinkoff.resistance.service.player.PlayerService

fun Application.gameModule() {
    val service: GameService by closestDI().instance()
    val controller: GameController by closestDI().instance()
    val playerService: PlayerService by closestDI().instance()
    routing {
        route("/game/create/{hostApiId}") {
            get {
                val apiId = call.parameters["hostApiId"]?.toLong()
                if (apiId != null) {
                    with(playerService.findByApiId(apiId)) {
                        if (!isActive()) {
                            val createdGame = service.create(id, name)
                            controller.addGameToActive(createdGame)
                            playerService.update(id, apiId, name, createdGame.id)
                            call.respond(HttpStatusCode.OK, createdGame.id)
                        } else {
                            call.respond(HttpStatusCode.NotAcceptable)
                        }
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest)
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
                        game.executeCommand(StartGameCommand(id, name))
                        call.respond(
                            HttpStatusCode.OK,
                            GameResponsesFormer.formTeamingInfoResponse(game, playerService)
                        )
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
                    game.executeCommand(ChoosePlayerForMissionCommand(id, name, candidate.id))
                    if (game.gameState == GameState.VOTING) {
                        call.respond(
                            HttpStatusCode.MultiStatus,
                            GameResponsesFormer.formVotingInfoResponse(game, playerService)
                        )
                    } else {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
        route("/game/voteforteam") {
            post {
                val request = call.receive<VoteForTeamRequest>()
                with(playerService.findByApiId(request.apiId)) {
                    val game = controller.getGameById(currentGameId ?: -1)
                    game.executeCommand(VoteForTeamCommand(id, name, request.agreement))
                    val basicInfoResponse = GameResponsesFormer.formBasicInfoResponse(game, playerService)
                    when (game.gameState) {
                        GameState.TEAMING -> call.respond(
                            HttpStatusCode.MultiStatus,
                            GameResponsesFormer.formTeamingInfoResponse(game, playerService)
                        )
                        GameState.MISSION -> call.respond(HttpStatusCode.OK, game.teammates.keys.toList())
                        GameState.END -> call.respond(
                            HttpStatusCode.Gone,
                            GameResponsesFormer.formEndGameResponse(game, playerService)
                        )
                        else -> call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
        route("/game/missionaction") {
            post {
                val request = call.receive<MissionActionRequest>()
                with(playerService.findByApiId(request.apiId)) {
                    val game = controller.getGameById(currentGameId ?: -1)
                    game.executeCommand(MissionActionCommand(id, name, request.action))
                    when (game.gameState) {
                        GameState.TEAMING -> call.respond(
                            HttpStatusCode.MultiStatus,
                            GameResponsesFormer.formTeamingInfoResponse(game, playerService)
                        )
                        GameState.END -> call.respond(
                            HttpStatusCode.Gone,
                            GameResponsesFormer.formEndGameResponse(game, playerService)
                        )
                        else -> call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}


fun DI.Builder.gameComponents() {
    bind<GameDao>() with singleton { GameDao(instance()) }
    bind<GameService>() with singleton { GameService(instance(), instance()) }
    bind<GameController>() with singleton { GameController() }
}