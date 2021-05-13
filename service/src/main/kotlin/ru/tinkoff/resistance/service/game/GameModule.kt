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
import ru.tinkoff.resistance.game.commands.*
import ru.tinkoff.resistance.model.request.*
import ru.tinkoff.resistance.model.response.InfoResponse
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
    val infoResponseFormer: InfoResponseFormer by closestDI().instance()
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
                call.respond(
                    HttpStatusCode.OK,
                    infoResponseFormer.formPairsApiIdsNames(controller.getGameById(request.gameId))
                )
            }
        }
        route("/game/leave/{apiId}") {
            get {
                val apiId = call.parameters["apiId"]?.toLong()
                if (apiId != null) {
                    val player = playerService.findByApiId(apiId)
                    val command = LeaveFromLobbyCommand(player.id, player.name)
                    controller.executeCommand(
                        player.currentGameId,
                        command
                    )
                    with(player) {
                        playerService.update(id, apiId, name, -1)
                    }
                    call.respond(
                        HttpStatusCode.OK,
                        infoResponseFormer.formPairsApiIdsNames(controller.getGameById(player.currentGameId))
                    )
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/game/start/{hostApiId}") {
            get {
                val apiId = call.parameters["hostApiId"]?.toLong()
                if (apiId != null) {
                    val player = playerService.findByApiId(apiId)
                    val command = StartGameCommand(player.id, player.name)
                    controller.executeCommand(
                        player.currentGameId,
                        command
                    )
                    respond(controller.getInfoResponse(player.currentGameId, infoResponseFormer))
                    controller.createGameHistory(player.currentGameId, gamesHistoryService)
                } else {
                    // Плохой запрос
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/game/chooseplayerformission") {
            post {
                val request = call.receive<ChoosePlayerForMissionRequest>()
                val player = playerService.findByApiId(request.leaderApiId)
                val command = ChoosePlayerForMissionCommand(
                    player.id,
                    player.name,
                    playerService.findByApiId(request.candidateApiId).id
                )
                controller.executeCommand(
                    player.currentGameId,
                    command
                )
                respond(controller.getInfoResponse(player.currentGameId, infoResponseFormer))
            }
        }
        route("/game/voteforteam") {
            post {
                val request = call.receive<VoteForTeamRequest>()
                val player = playerService.findByApiId(request.apiId)
                val command = VoteForTeamCommand(player.id, player.name, request.agreement)
                controller.executeCommand(
                    player.currentGameId,
                    command
                )
                respond(controller.getInfoResponse(player.currentGameId, infoResponseFormer))
            }
        }
        route("/game/missionaction") {
            post {
                val request = call.receive<MissionActionRequest>()
                val player = playerService.findByApiId(request.apiId)
                val command = MissionActionCommand(player.id, player.name, request.action)
                controller.executeCommand(
                    player.currentGameId,
                    command
                )
                respond(controller.getInfoResponse(player.currentGameId, infoResponseFormer))
            }
        }
        route("/game/close/{apiId}") {
            get {
                val apiId = call.parameters["apiId"]?.toLong()
                if (apiId != null) {
                    val player = playerService.findByApiId(apiId)
                    val command = EarlyFinishGameCommand(player.id, player.name)
                    respond(controller.getInfoResponse(player.currentGameId, infoResponseFormer))
                    controller.executeCommand(player.currentGameId, command)
                    controller.closeGame(playerService.findByApiId(apiId).currentGameId, service, playerService)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/game/getallactiveusers") {
            get {
                call.respond(HttpStatusCode.OK, controller.getAllActivePlayersApiIds(playerService))
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.respond(respondPair: Pair<HttpStatusCode, InfoResponse>) {
    call.respond(respondPair.first, respondPair.second)
}

fun DI.Builder.gameComponents() {
    bind<GameDao>() with singleton { GameDao(instance()) }
    bind<GameService>() with singleton { GameService(instance(), instance()) }
    bind<GameController>() with singleton { GameController() }
    bind<GamesHistoryDao>() with singleton { GamesHistoryDao(instance()) }
    bind<GamesHistoryService>() with singleton { GamesHistoryService(instance(), instance()) }
    bind<InfoResponseFormer>() with singleton { InfoResponseFormer(instance()) }
}