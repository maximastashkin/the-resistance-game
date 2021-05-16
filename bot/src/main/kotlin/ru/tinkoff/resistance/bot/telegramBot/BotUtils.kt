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
import ru.tinkoff.resistance.model.game.MissionResult
import ru.tinkoff.resistance.model.game.Role
import ru.tinkoff.resistance.model.game.VoteResult
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

fun getPlayersNames(players: List<Pair<Long, String>>): String {
    var string = ""
    players.forEach {
        string += it.second + "\n"
    }
    return string
}

fun Bot.joinLobby(players: List<Pair<Long, String>>) {
    val newPlayer = players.last()
    val otherPlayers = players - newPlayer
    this.sendMsg(
        newPlayer.first,
        "Список игроков в лобби:\n${getPlayersNames(otherPlayers)}"
    )
    otherPlayers.forEach {
        this.sendMsg(it.first, "${newPlayer.second} зашел в лобби")
    }
}

fun Bot.leaveLobby(leaveInfo: Pair<List<Pair<Long, String>>, Boolean>, leaver: Pair<Long, String>) {
    val players = leaveInfo.first - leaver
    val isHost = leaveInfo.second
    players.forEach {
        this.sendMsg(it.first, "${leaver.second} покинул лобби")
    }
    if (isHost) {
        players.forEach {
            this.sendMsg(it.first, "Игра отменяется, так как хост покинул лобби", Buttons.START_BUTTONS)
        }
    }
}

fun Bot.startGame(infoResponse: InfoResponse) {
    val traitors = infoResponse.traitors
    val notTraitors = infoResponse.notTraitors
    val players = (traitors + notTraitors).shuffled()
    val leader = infoResponse.missionLeader
    val playerNames = "Список игроков: \n${getPlayersNames(players)}"
    val leaderInfo = "Лидер первого раунда ${leader.second}."
    players.forEach {
        this.sendMsg(it.first, playerNames)
        this.sendMsg(it.first, leaderInfo)
    }
    val traitorsNames = "Список предателей: \n${getPlayersNames(traitors)}"
    traitors.forEach {
        this.sendMsg(it.first, "Вы предатель")
        this.sendMsg(it.first, traitorsNames)
    }
    notTraitors.forEach {
        this.sendMsg(it.first, "Вы в сопротивлении")
    }
    this.notifyLeader(leader, players)
}

fun Bot.notifyLeader(leader: Pair<Long, String>, players: List<Pair<Long, String>>) {
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
    val teammatesNames = "Список игроков на миссию: \n${getPlayersNames(teammates)}"
    when (infoResponse.gameState) {
        GameState.TEAMING -> {
            val players = (traitors + notTraitors - teammates).shuffled()
            this.sendMsg(
                leaderId,
                "Игрок успешно выбран\n $teammatesNames",
                Buttons.getTeamingButtons(players)
            )
        }
        GameState.VOTING -> {
            val players = (traitors + notTraitors).shuffled()
            this.sendMsg(leaderId, "Последний игрок успешно выбран")
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
    val players = (traitors + notTraitors).shuffled()
    val votes = infoResponse.votesResults.shuffled()
    if (infoResponse.gameState != GameState.VOTING) {
        this.sendPlayersVotes(players, votes)
    }
    when (infoResponse.gameState) {
        GameState.MISSION -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование закончилось успешно")
                this.sendMsg(it.first, "Миссия начинается")
            }
            teammates.forEach {
                this.sendMsg(
                    it.first,
                    "Решите исход миссии",
                    if (traitors.contains(it)) Buttons.MISSION_BUTTONS else Buttons.MISSION_FOR_NOT_TRAITORS
                )
            }
        }
        GameState.TEAMING -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование решило поменять команду")
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.notifyLeader(leader, players)
        }
        GameState.END -> {
            players.forEach {
                this.sendMsg(it.first, "Голосование прервалось 5 раз")
            }
            gameOver(infoResponse, client, config)
        }
    }
}

fun Bot.sendPlayersVotes(players: List<Pair<Long, String>>, votes: List<Pair<String, VoteResult>>) {
    players.forEach {
        votes.forEach { el ->
            when (el.second) {
                VoteResult.AGREE -> {
                    this.sendMsg(it.first, "${el.first} проголосовал за")
                }
                VoteResult.DISAGREE -> {
                    this.sendMsg(it.first, "${el.first} проголосовал против")
                }
                else -> {
                    this.sendMsg(it.first, "Голос ${el.first} не учтен")
                }
            }
        }
    }
}

fun Bot.mission(infoResponse: InfoResponse, client: HttpClient, config: AppConfig) {
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val notTraitors = infoResponse.notTraitors
    val countSuccessMissions = infoResponse.countSuccessedMissions
    val countFailedMissions = infoResponse.countFailedMissions
    val players = (traitors + notTraitors).shuffled()
    val successVotes = infoResponse.lastMissionVotes.first
    val failVotes = infoResponse.lastMissionVotes.second
    val missionResult = infoResponse.lastMissionResult
    when (infoResponse.gameState) {
        GameState.TEAMING -> {
            sendMissionResult(players,
                missionResult,
                successVotes,
                failVotes,
                countSuccessMissions,
                countFailedMissions)
            players.forEach {
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.notifyLeader(leader, players)
        }
        GameState.END -> {
            sendMissionResult(players,
                missionResult,
                successVotes,
                failVotes,
                countSuccessMissions,
                countFailedMissions)
            this.gameOver(infoResponse, client, config)
        }
    }
}

fun Bot.sendMissionResult(
    players: List<Pair<Long, String>>,
    missionResult: MissionResult,
    successVotes: Int,
    failVotes: Int,
    countSuccessMissions: Int,
    countFailedMissions: Int
) {
    players.forEach {
        if(missionResult == MissionResult.SUCCESS){
            this.sendMsg(it.first, "Миссия прошла успешно")
        } else if(missionResult == MissionResult.FAIL){
            this.sendMsg(it.first, "Произошел саботаж! Миссия провалилась")
        }
        this.sendMsg(it.first, "Количество игроков проголосовавших за успех миссии: $successVotes")
        this.sendMsg(it.first, "Количество игроков проголосовавших за провал миссии: $failVotes")
        this.sendMsg(it.first, "Счет: сопротивление $countSuccessMissions, предатели $countFailedMissions")
    }
}

fun Bot.gameOver(infoResponse: InfoResponse, client: HttpClient, config: AppConfig) {
    val traitors = infoResponse.traitors
    val notTraitors = infoResponse.notTraitors
    val players = (traitors + notTraitors).shuffled()
    val traitorsNames = "Список предателей: \n${getPlayersNames(traitors)}"
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
                this.sendMsg(it.first, "Игра досрочно закончилась\n$traitorsNames", Buttons.START_BUTTONS)
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
        this.sendMsg(it.first, "К сожалению, Вы проиграли. Сыграем еще?", Buttons.START_BUTTONS)
    }
}

fun closeGame(apiId: Long, client: HttpClient, config: AppConfig) {
    runBlocking {
        client.get<HttpResponse>(config.server.url + config.server.closeRoute + "/$apiId")
    }
}