package ru.tinkoff.resistance.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.webhook
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import ru.tinkoff.resistance.model.request.ChoosePlayerForMissionRequest
import ru.tinkoff.resistance.model.request.JoinGameRequest
import ru.tinkoff.resistance.model.request.PlayerCreateRequest
import ru.tinkoff.resistance.model.response.TeamingInfoResponse
import ru.tinkoff.resistance.model.response.VotingInfoResponse

fun botModule(config: AppConfig, client: HttpClient): Bot {
    return bot {
        token = config.telegram.token

        webhook {
            url = config.telegram.webhookUrl
        }

        dispatch {
            command("test") {
                this.bot.sendMsg(message.chat.id, "test", Buttons.TEST_BUTTONS)
            }

            command("start") {
                runBlocking {
                    val response = client.post<HttpStatement>(config.server.url + "player") {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = PlayerCreateRequest(message.chat.id, message.from!!.firstName)
                    }
                    when (response.execute().status) {
                        HttpStatusCode.OK -> {
                            bot.sendMsg(message.chat.id, "Добро пожаловать!", Buttons.START_BUTTONS)
                        }
                        HttpStatusCode.NotAcceptable -> {
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
                val response: HttpResponse
                runBlocking {
                    response = client.get(config.server.url + "game/create/${id}") {
                        method = HttpMethod.Get
                        contentType(ContentType.Application.Json)
                    }
                    when (response.status) {
                        HttpStatusCode.OK -> {
                            var gameId = response.receive<Long>()
                            bot.sendMsg(id, "Игра успешно создана. Номер игры: $gameId", Buttons.START_GAME)
                        }
                        HttpStatusCode.NotAcceptable -> {
                            bot.sendMsg(id, "Вы уже в игре")
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                        }
                        else -> {
                            bot.sendMsg(id, "Что-то пошло не так")
                        }
                    }
                }
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("join") {
                this.bot.sendMsg(callbackQuery.from.id, "Введите id игры /join id")
                bot.deleteMsg(callbackQuery)
            }

            command("join"){
                val strings = message.text!!.split(" ")
                val answer = if(strings.size == 2){
                    try{
                        val lobbyId = strings[1].toInt()
                        val response: HttpResponse
                        runBlocking {
                            response = client.post(config.server.url + "game/join") {
                                method = HttpMethod.Post
                                contentType(ContentType.Application.Json)
                                body = JoinGameRequest(message.chat.id, lobbyId)
                            }
                        }
                        when(response.status){
                            HttpStatusCode.OK -> {
                                "Вы успешно зашли в игру. Номер игры: $lobbyId"
                            }
                            HttpStatusCode.NotAcceptable -> {
                                "Вы уже в игре"
                            }
                            HttpStatusCode.NotFound -> {
                                "Вы не найдены в базе"
                            }
                            else -> "Что-то пошло не так"
                        }
                    } catch (ex: NumberFormatException){
                        "Id игры должен быть числом"
                    }
                } else {
                    "Команда введена не правильно"
                }
                this.bot.sendMsg(message.chat.id, answer)
            }

            callbackQuery("start") {
                val id = callbackQuery.from.id
                val response: HttpResponse
                runBlocking {
                    response = client.get(config.server.url + "game/start/$id") {
                        method = HttpMethod.Get
                        contentType(ContentType.Application.Json)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            bot.sendMsg(id, "Игра успешно запущена")
                            val teamInfo = response.receive<TeamingInfoResponse>()
                            println(teamInfo)
                            bot.sendMsg(teamInfo.missionLeaderApiId,
                                "Вы лидер! Выберите 3 игроков в команду",
                                Buttons.TEAMING_BUTTONS
                                )
                        }
                        HttpStatusCode.NotAcceptable -> {
                            bot.sendMsg(id, "Вы уже в игре")
                            // CommandErrorCode
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                        }
                        else -> {
                            bot.sendMsg(id, "Что-то пошло не так")
                        }
                    }
                }
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("teaming"){
                bot.sendMsg(callbackQuery.from.id, "Чтобы выбрать игрока /invite id")
                bot.deleteMsg(callbackQuery)
            }

            command("invite"){
                val strings = message.text!!.split(" ")
                if(strings.size == 2){
                    try{
                        val playerId = strings[1].toLong()
                        val response: HttpResponse
                        runBlocking {
                            response = client.post(config.server.url + "game/chooseplayerformission") {
                                method = HttpMethod.Post
                                contentType(ContentType.Application.Json)
                                body = ChoosePlayerForMissionRequest(message.from!!.id, playerId)
                            }
                            when(response.status){
                                HttpStatusCode.OK -> {
                                    bot.sendMsg(message.chat.id, "Игрок успешного выбран")
                                }
                                HttpStatusCode.MultiStatus -> {
                                    val votingInfo = response.receive<VotingInfoResponse>()
                                    val team: String = ""
                                    votingInfo.playersApiIds.forEach{
                                        bot.sendMsg(it,
                                            "Команда сформирована из $team. Ваше мнение?",
                                            Buttons.VOTING_BUTTONS)
                                    }

                                }
                                HttpStatusCode.NotAcceptable -> {
                                    bot.sendMsg(message.chat.id, "Вы уже в игре")
                                }
                                HttpStatusCode.NotFound -> {
                                    bot.sendMsg(message.chat.id, "Вы не найдены в базе")
                                }
                                else -> "Что-то пошло не так"
                            }
                        }
                    } catch (ex: NumberFormatException){
                        "Id игрока должен быть числом"
                    }
                } else {
                    "Команда введена не правильно"
                }
            }


            callbackQuery("voting") {
                this.bot.sendMsg(callbackQuery.from.id,
                    "Do you agree with such a team: ....",
                    Buttons.VOTING_BUTTONS
                )
                this.bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteYes") {
                this.bot.sendMsg(callbackQuery.from.id, "YES")
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteNo") {
                this.bot.sendMsg(callbackQuery.from.id, "NO")
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("mission"){
                this.bot.sendMsg(callbackQuery.from.id, "Choose the outcome of the mission", Buttons.MISSION_BUTTONS)
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteSuccess") {
                this.bot.sendMsg(callbackQuery.from.id, text = "SUCCESS")
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteFail") {
                this.bot.sendMsg(callbackQuery.from.id, text = "FAIL")
                bot.deleteMsg(callbackQuery)
            }
        }
    }
}

fun Bot.deleteMsg(callbackQuery: CallbackQuery){
    this.deleteMessage(
        chatId = ChatId.fromId(callbackQuery.from.id),
        messageId = callbackQuery.message!!.messageId
    )
}

fun Bot.sendMsg(chatId: Long, text: String, replyMarkup: ReplyMarkup? = null){
    this.sendMessage(
        chatId = ChatId.fromId(chatId),
        text = text,
        replyMarkup = replyMarkup
    )
}


