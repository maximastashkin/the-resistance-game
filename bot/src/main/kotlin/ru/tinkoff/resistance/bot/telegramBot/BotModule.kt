package ru.tinkoff.resistance.bot.telegramBot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.webhook
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import ru.tinkoff.resistance.bot.AppConfig
import ru.tinkoff.resistance.errocodes.CommandErrorCode
import ru.tinkoff.resistance.model.request.*
import ru.tinkoff.resistance.model.response.InfoResponse

fun botModule(config: AppConfig, client: HttpClient): Bot {
    return bot {
        token = config.telegram.token

        webhook {
            url = config.telegram.webhookUrl
        }

        dispatch {
            command("start") {
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + config.server.startRoute) {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = PlayerCreateRequest(message.chat.id, message.from!!.firstName)
                    }
                    when (response.status) {
                        HttpStatusCode.Created -> {
                            bot.sendMsg(message.chat.id, "Добро пожаловать!", Buttons.START_BUTTONS)
                        }
                        HttpStatusCode.InternalServerError -> {
                            bot.sendMsg(message.chat.id, "Вы уже зарегистрированы!", Buttons.START_BUTTONS)
                        }
                        else -> {
                            bot.sendMsg(message.chat.id, "Что-то пошло не так")
                        }
                    }
                }
            }

            callbackQuery("create") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + config.server.createRoute) {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = CreateGameRequest(id)
                    }
                    when (response.status) {
                        HttpStatusCode.Created -> {
                            val gameId = response.receive<Long>()
                            bot.sendMsg(id, "Игра успешно создана. Номер игры: $gameId", Buttons.START_GAME)
                            bot.deleteLastMsg(callbackQuery)
                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                        }
                        else -> {
                            bot.sendMsg(id, "Что-то пошло не так")
                        }
                    }
                }
            }

            callbackQuery("join") {
                this.bot.sendMsg(callbackQuery.from.id, "Введите id игры /join id")
                bot.deleteLastMsg(callbackQuery)
            }

            command("join") {
                val strings = message.text!!.split(" ")
                if (strings.size == 2) {
                    try {
                        val lobbyId = strings[1].toInt()
                        runBlocking {
                            val response = client.post<HttpResponse>(config.server.url + config.server.joinRoute) {
                                method = HttpMethod.Post
                                contentType(ContentType.Application.Json)
                                body = JoinGameRequest(message.chat.id, lobbyId)
                            }
                            when (response.status) {
                                HttpStatusCode.OK -> {
                                    val players = response.receive<List<Pair<Long, String>>>()
                                    bot.joinLobby(players, lobbyId)
                                }
                                HttpStatusCode.InternalServerError -> {
                                    val commandErrorCode = response.receive<CommandErrorCode>()
                                    bot.sendMsg(message.chat.id, commandErrorCode.getMessage())
                                }
                                HttpStatusCode.NotFound -> {
                                    bot.sendMsg(message.chat.id, "Вы не найдены в базе")
                                }
                                else -> bot.sendMsg(message.chat.id, "Что-то пошло не так")
                            }
                        }

                    } catch (ex: NumberFormatException) {
                        bot.sendMsg(message.chat.id, "Неправильный ID игры")
                    }
                } else {
                    bot.sendMsg(message.chat.id, "Команда введена не правильно")
                }
            }

            callbackQuery("leave") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.get<HttpResponse>(config.server.url + config.server.leaveRoute + id) {
                        method = HttpMethod.Get
                        contentType(ContentType.Application.Json)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val players = response.receive<Pair<List<Pair<Long, String>>, Boolean>>()
                            bot.leaveLobby(players, Pair(id, callbackQuery.from.firstName))
                            bot.deleteLastMsg(callbackQuery)
                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                        }
                        else -> {
                            bot.sendMsg(id, "Что-то пошло не так")
                        }
                    }
                }
            }

            callbackQuery("start") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.get<HttpResponse>(config.server.url + config.server.gameStartRoute + id) {
                        method = HttpMethod.Get
                        contentType(ContentType.Application.Json)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            bot.sendMsg(id, "Игра успешно запущена")
                            bot.deleteLastMsg(callbackQuery)
                            val infoResponse = response.receive<InfoResponse>()
                            bot.startGame(infoResponse)

                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                        }
                        else -> {
                            bot.sendMsg(id, "Что-то пошло не так")
                        }
                    }
                }
            }

            callbackQuery("invite") {
                val id = callbackQuery.from.id
                val candidateId = callbackQuery.data.split(" ")[1].toLong()
                runBlocking {
                    val response =
                        client.post<HttpResponse>(config.server.url + config.server.choosePlayerForMission) {
                            method = HttpMethod.Post
                            contentType(ContentType.Application.Json)
                            body = ChoosePlayerForMissionRequest(id, candidateId)
                        }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val infoResponse = response.receive<InfoResponse>()
                            bot.choosePlayer(infoResponse)
                            bot.deleteLastMsg(callbackQuery)
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Игрок не найден в базе")
                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        else -> bot.sendMsg(id, "Что-то пошло не так")
                    }
                }
            }

            callbackQuery("vote") {
                val answer = callbackQuery.data == "voteYes"
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + config.server.voteForTeam) {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = VoteForTeamRequest(id, answer)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val infoResponse = response.receive<InfoResponse>()
                            bot.voteForTeam(infoResponse, client, config)
                            bot.deleteLastMsg(callbackQuery)
                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        else -> bot.sendMsg(id, "Что-то пошло не так")

                    }
                }
                bot.deleteLastMsg(callbackQuery)
            }

            callbackQuery("mission") {
                val answer = callbackQuery.data == "missionSuccess"
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + config.server.missionAction) {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = MissionActionRequest(id, answer)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val infoResponse = response.receive<InfoResponse>()
                            bot.mission(infoResponse, client, config)
                            bot.deleteLastMsg(callbackQuery)
                        }
                        HttpStatusCode.InternalServerError -> {
                            val commandErrorCode = response.receive<CommandErrorCode>()
                            bot.sendMsg(id, commandErrorCode.getMessage())
                        }
                        else -> bot.sendMsg(id, "Что-то пошло не так")
                    }
                }
            }
        }
    }
}
