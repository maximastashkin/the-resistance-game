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
import ru.tinkoff.resistance.game.commands.CommandExecutionException
import ru.tinkoff.resistance.game.commands.JoinGameCommand
import ru.tinkoff.resistance.game.commands.StartGameCommand
import ru.tinkoff.resistance.requests.models.game.JoinGameRequest
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
                    runCatching {
                        playerService.findByApiId(apiId)
                    }.onSuccess {
                        if (!it.isActive()) {
                            val createdGame = service.create(it.id, it.name)
                            controller.addGameToActive(createdGame)
                            playerService.update(it.id, it.apiId, it.name, createdGame.id)
                            call.respond(HttpStatusCode.OK)
                        } else {
                            // Чел уже в игре
                            call.respond(HttpStatusCode.NotAcceptable)
                        }
                    }.onFailure {
                        // Чел не найден в бд
                        call.respond(HttpStatusCode.NotFound)
                    }
                } else {
                    // Плохой запрос
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
        route("/game/join") {
            post {
                val request = call.receive<JoinGameRequest>()
                runCatching {
                    playerService.findByApiId(request.apiId)
                }.onSuccess {
                    runCatching {
                        controller.getGameById(request.gameId).executeCommand(JoinGameCommand(it.id, it.name))
                        playerService.update(it.id, it.apiId, it.name, request.gameId)
                        call.respond(HttpStatusCode.OK)
                    }.onFailure {
                        // Такой игры нет или чел уже в игре/игра уже запущена/слотов нет
                        call.respond(
                            when (it) {
                                is CommandExecutionException -> HttpStatusCode.NotAcceptable // тут еще дописывать
                                is Exception -> HttpStatusCode.NotFound
                                else -> HttpStatusCode.BadRequest
                            }
                        )
                    }
                }.onFailure {
                    // Чел не найден в бд
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
        route("/game/start/{hostApiId}") {
            get {
                val apiId = call.parameters["hostApiId"]?.toLong()
                if (apiId != null) {
                    runCatching {
                        playerService.findByApiId(apiId)
                    }.onSuccess {
                        runCatching {
                            controller.getGameById(it.currentGameId ?: -1)
                                .executeCommand(StartGameCommand(it.id, it.name))
                            call.respond(HttpStatusCode.OK)
                        }.onFailure {
                            // Игры нет или чел не хост/уже запущена/недостаточно игроков
                            call.respond(HttpStatusCode.NotAcceptable)
                        }
                    }.onFailure {
                        // Чел не найден в бд
                        call.respond(HttpStatusCode.NotFound)
                    }
                } else {
                    // Плохой запрос
                    call.respond(HttpStatusCode.BadRequest)
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