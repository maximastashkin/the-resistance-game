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
import ru.tinkoff.resistance.model.request.*
import ru.tinkoff.resistance.model.response.InfoResponse

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
                    val response = client.post<HttpResponse>(config.server.url + "player") {
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
                    val response = client.post<HttpResponse>(config.server.url + "game/create/${id}") {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = CreateGameRequest(id)
                    }
                    when (response.status) {
                        HttpStatusCode.Created -> {
                            var gameId = response.receive<Long>()
                            bot.sendMsg(id, "Игра успешно создана. Номер игры: $gameId", Buttons.START_GAME)
                        }
                        HttpStatusCode.InternalServerError -> {
                            bot.sendMsg(id, "Вы уже в игре")
                            //CommandErrorCode.ALREADY_IN_GAME
                        }
                        HttpStatusCode.NotFound -> {
                            bot.sendMsg(id, "Вы не найдены в базе")
                            // chatId
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
                if(strings.size == 2){
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
                                bot.sendMsg(message.chat.id,"Вы успешно зашли в игру. Номер игры: $lobbyId")
                            }
                            HttpStatusCode.InternalServerError -> {
                                bot.sendMsg(message.chat.id,"Вы уже в игре")
                                // CommandErrorCode.ALREADY_IN_GAME
                            }
                            HttpStatusCode.NotFound -> {
                                bot.sendMsg(message.chat.id,"Вы не найдены в базе")
                                //telegram id
                            }
                            else -> bot.sendMsg(message.chat.id,"Что-то пошло не так")
                        }
                    } catch (ex: NumberFormatException){
                        bot.sendMsg(message.chat.id,"Id игры должен быть числом")
                    }
                } else {
                    bot.sendMsg(message.chat.id,"Команда введена не правильно")
                }
            }

            callbackQuery("start") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.get<HttpResponse>(config.server.url + "game/start/$id") {
                        method = HttpMethod.Get
                        contentType(ContentType.Application.Json)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            bot.sendMsg(id, "Игра успешно запущена")
                            val teamInfo = response.receive<InfoResponse>()
                            // Вывод всех игроков всем
                            // Рисуем кнопки для лидера
                            // Переходим в тиминг
                            bot.sendMsg(teamInfo.missionLeaderApiId,
                                "Вы лидер! Выберите 3 игроков в команду",
                                Buttons.TEAMING_BUTTONS
                                )
                        }
                        HttpStatusCode.InternalServerError-> {
                            bot.sendMsg(id, "Вы уже в игре")
                            // CommandErrorCode очень много
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
                        runBlocking {
                            val response = client.post<HttpResponse>(config.server.url + "game/chooseplayerformission") {
                                method = HttpMethod.Post
                                contentType(ContentType.Application.Json)
                                body = ChoosePlayerForMissionRequest(message.from!!.id, playerId)
                            }
                            when(response.status){
                                HttpStatusCode.OK -> {
                                    bot.sendMsg(message.chat.id, "Игрок успешного выбран")
                                    // Проверить статус игры и если все проголосовали перейти в миссию или голосование
                                }
                                HttpStatusCode.NotFound -> {
                                    bot.sendMsg(message.chat.id, "Игрок не найден в базе")
                                    // Ид
                                }
                                HttpStatusCode.InternalServerError-> {
                                    bot.sendMsg(message.chat.id, "Ошибочка")
                                    // CommandError
                                }
                                else -> bot.sendMsg(message.chat.id,"Что-то пошло не так")
                            }
                        }
                    } catch (ex: NumberFormatException){
                        bot.sendMsg(message.chat.id,"Id игрока должен быть числом")
                    }
                } else {
                    bot.sendMsg(message.chat.id,"Команда введена не правильно")
                }
            }

            callbackQuery("voteYes") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + "game/voteforteam"){
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = VoteForTeamRequest(id, true)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            // InfoResponce
                        }
                        HttpStatusCode.InternalServerError -> {
                            // Обработка ошибки
                        }
                    }
                }
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteNo") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + "game/voteforteam"){
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = VoteForTeamRequest(id, false)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            // InfoResponce
                        }
                        HttpStatusCode.InternalServerError -> {
                            // Обработка ошибки
                        }
                    }
                }
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("mission"){
                this.bot.sendMsg(callbackQuery.from.id, "Choose the outcome of the mission", Buttons.MISSION_BUTTONS)
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteSuccess") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + "game/voteforteam"){
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = MissionActionRequest(id, true)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            // InfoResponce
                        }
                        HttpStatusCode.InternalServerError -> {
                            // Обработка ошибки
                        }
                    }
                }
                bot.deleteMsg(callbackQuery)
            }

            callbackQuery("voteFail") {
                val id = callbackQuery.from.id
                runBlocking {
                    val response = client.post<HttpResponse>(config.server.url + "game/voteforteam"){
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = MissionActionRequest(id, false)
                    }
                    when(response.status){
                        HttpStatusCode.OK -> {
                            // InfoResponce
                        }
                        HttpStatusCode.InternalServerError -> {
                            // Обработка ошибки
                        }
                    }
                }
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