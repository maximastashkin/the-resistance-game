package ru.tinkoff.resistance.bot.telegramBot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyMarkup
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import ru.tinkoff.resistance.bot.AppConfig
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.model.game.Role
import ru.tinkoff.resistance.model.response.InfoResponse


fun Bot.deleteLastMsg(callbackQuery: CallbackQuery) {
    this.deleteMessage(
        chatId = ChatId.fromId(callbackQuery.from.id),
        messageId = callbackQuery.message!!.messageId
    )
}

fun Bot.sendMsg(chatId: Long, text: String, replyMarkup: ReplyMarkup? = null) =
    this.sendMessage(
        chatId = ChatId.fromId(chatId),
        text = text,
        replyMarkup = replyMarkup
    )

fun getNames(list: List<Pair<Long, String>>): String {
    var string = ""
    list.forEach {
        string += it.second + "\n"
    }
    return string
}

fun Bot.joinLobby(players: List<Pair<Long, String>>, lobbyId: Int) {
    val newPlayer = players.last()
    val otherPlayers = players - newPlayer
    this.sendMsg(
        newPlayer.first,
        "Вы успешно зашли в игру. Номер игры: $lobbyId",
        Buttons.LOBBY_BUTTONS
    )
    this.sendMsg(
        newPlayer.first,
        "Список игроков в лобби:\n${getNames(otherPlayers)}"
    )
    otherPlayers.forEach {
        this.sendMsg(it.first, "${newPlayer.second} зашел в лобби")
    }
}

fun Bot.leaveLobby(players: List<Pair<Long, String>>, leaver: Pair<Long, String>) {
    this.sendMsg(leaver.first, "Вы успешно покинули лобби", Buttons.START_BUTTONS)
    players.forEach {
        this.sendMsg(it.first, "${leaver.second} покинул лобби")
    }
}

fun Bot.startGame(infoResponse: InfoResponse) {
    val traitors = infoResponse.traitors
    val notTraitors = infoResponse.notTraitors
    val players = (traitors + notTraitors).shuffled()
    val leader = infoResponse.missionLeader
    val playerNames = "Список игроков: \n${getNames(players)}"
    val leaderInfo = "Лидер первого раунда ${leader.second}."
    players.forEach {
        this.sendMsg(it.first, playerNames)
        this.sendMsg(it.first, leaderInfo)
    }
    val traitorsNames = "Список предателей: \n${getNames(traitors)}"
    traitors.forEach {
        this.sendMsg(it.first, "Вы предатель.")
        this.sendMsg(it.first, traitorsNames)
    }
    notTraitors.forEach {
        this.sendMsg(it.first, "Вы сопротивленец.")
    }
    this.sendMsg(
        leader.first, "Вы лидер. Набирайте команду",
        Buttons.getTeamingButtons(players - leader)
    )
}

fun Bot.choosePlayer(infoResponse: InfoResponse) {
    val traitors = infoResponse.traitors
    val leaderId = infoResponse.missionLeader.first
    val notTraitors = infoResponse.notTraitors
    val teammates = infoResponse.teammates
    val teammatesNames = "Список игроков на миссию: \n${getNames(teammates)}"

    when (infoResponse.gameState) {
        GameState.TEAMING -> {
            val players = traitors + notTraitors - teammates
            this.sendMsg(
                leaderId,
                "Игрок успешно выбран.\n $teammatesNames",
                Buttons.getTeamingButtons(players.shuffled())
            )
        }
        GameState.VOTING -> {
            val players = traitors + notTraitors
            this.sendMsg(
                leaderId,
                "Последний игрок успешно выбран."
            )
            players.forEach {
                this.sendMsg(it.first, teammatesNames)
                this.sendMsg(it.first, "Ваше мнение?", Buttons.VOTING_BUTTONS)
            }
        }
        else -> {
            this.sendMsg(leaderId, "Что-то пошло не так")
        }
    }
}

fun Bot.voteForTeam(infoResponse: InfoResponse, client: HttpClient, config: AppConfig) {
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val notTraitors = infoResponse.notTraitors
    val teammates = infoResponse.teammates
    val players = traitors + notTraitors
    when (infoResponse.gameState) {
        GameState.MISSION -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование закончилось успешно")
                this.sendMsg(it.first, "Миссия начинается")
            }
            teammates.forEach {
                this.sendMsg(it.first, "Решите исход миссии", Buttons.MISSION_BUTTONS)
            }
        }
        GameState.TEAMING -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование решило поменять команду")
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.sendMsg(
                leader.first, "Вы лидер. Набирайте команду",
                Buttons.getTeamingButtons(players.shuffled())
            )
        }
        GameState.END -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование прервалось 5 раз. Игра окончена")
            }
            gameOver(infoResponse, client, config)
        }
    }
}

fun Bot.mission(infoResponse: InfoResponse, client: HttpClient, config: AppConfig) {
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val notTraitors = infoResponse.notTraitors
    val countSuccessMissions = infoResponse.countSuccessedMissions
    val countFailedMissions = infoResponse.countFailedMissions
    val players = traitors + notTraitors
    when (infoResponse.gameState) {
        GameState.TEAMING -> {
            players.forEach {
                this.sendMsg(it.first, "Миссия завершилась c исходом: ${infoResponse.lastMissionResult}")
                this.sendMsg(it.first, "Счет сопротивленцы $countSuccessMissions, предатели $countFailedMissions")
            }
            players.forEach {
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.sendMsg(
                leader.first, "Вы лидер. Набирайте команду",
                Buttons.getTeamingButtons(players.shuffled())
            )
        }
        GameState.END -> {
            players.forEach {
                this.sendMsg(it.first, "Миссия завершилась c исходом: ${infoResponse.lastMissionResult}")
                this.sendMsg(it.first, "Счет сопротивленцы $countSuccessMissions, предатели $countFailedMissions")
            }
            this.gameOver(infoResponse, client, config)
        }
    }
}

fun Bot.gameOver(infoResponse: InfoResponse, client: HttpClient, config: AppConfig) {
    val traitors = infoResponse.traitors
    val notTraitors = infoResponse.notTraitors
    val players = traitors + notTraitors
    val traitorsNames = "Список предателей: \n${getNames(traitors)}"
    when (infoResponse.winner) {
        Role.RESISTANCE -> {
            players.forEach {
                this.sendMsg(it.first, "Игра закончилась. Победила команда сопротивления\n$traitorsNames")
            }
            this.sendResults(notTraitors, traitors)
        }
        Role.TRAITOR -> {
            players.forEach {
                this.sendMsg(it.first, "Игра закончилась. Сопротивление потерпело неудачу\n$traitorsNames")
            }
            this.sendResults(traitors, notTraitors)
        }
        else -> {
            players.forEach {
                this.sendMsg(it.first, "Игра закончилась по тех причинам")
            }
        }
    }
    closeGame(players[0].first, client, config)
}

fun Bot.sendResults(winners: List<Pair<Long, String>>, losers: List<Pair<Long, String>>) {
    winners.forEach {
        this.sendMsg(it.first, "Вы одержали победу! Сыграем еще?", Buttons.START_BUTTONS)
    }
    losers.forEach {
        this.sendMsg(it.first, "К сожалению, Вы проиграли! Сыграем еще?", Buttons.START_BUTTONS)
    }
}

fun closeGame(apiId: Long, client: HttpClient, config: AppConfig) {
    runBlocking {
        client.get<HttpResponse>(config.server.url + config.server.closeRoute + "/$apiId")
    }
}