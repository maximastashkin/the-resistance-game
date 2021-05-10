package ru.tinkoff.resistance.bot.telegramBot

import com.github.kotlintelegrambot.Bot
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.model.response.InfoResponse

fun Bot.startGame(infoResponse: InfoResponse) {
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val leaderInfo = "Лидер первого раунда ${leader.second}."
    var traitorsNames = "Список предателей: "
    traitors.forEach {
        traitorsNames += it.second + " "
    }
    traitors.forEach{
        this.sendMsg(it.first, "Вы предатель.")
        this.sendMsg(it.first, "$traitorsNames.")
        this.sendMsg(it.first, leaderInfo)
    }
    val notTraitors = infoResponse.notTraitors
    notTraitors.forEach{
        this.sendMsg(it.first, "Вы сопротивленец.")
        this.sendMsg(it.first, leaderInfo)
    }
    val players = (traitors + notTraitors)
    this.sendMsg(
        leader.first, "Вы лидер. Набирайте команду",
        Buttons.getTeamingButtons(players.shuffled())
    )
}

fun Bot.choosePlayer(infoResponse: InfoResponse) {
    val traitors = infoResponse.traitors
    val leaderId = infoResponse.missionLeader.first
    val notTraitors = infoResponse.notTraitors
    val teammates = infoResponse.teammates
    var teammatesNames = "Список игроков на миссию: "
    teammates.forEach {
        teammatesNames += it.second + " "
    }
    val players = traitors + notTraitors - teammates
    when (infoResponse.gameState) {
        GameState.TEAMING -> {
            this.sendMsg(leaderId,
                "Игрок успешно выбран.\n $teammatesNames",
                Buttons.getTeamingButtons(players.shuffled()))
        }
        GameState.VOTING -> {
            this.sendMsg(leaderId,
                "Последний игрок успешно выбран.")
            (players + teammates).forEach{
                this.sendMsg(it.first, teammatesNames)
                this.sendMsg(it.first, "Ваше мнение?", Buttons.VOTING_BUTTONS)
            }
        }
        else -> {
            this.sendMsg(leaderId, "Что-то пошло не так")
        }
    }
}

fun Bot.voteForTeam(infoResponse: InfoResponse){
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val notTraitors = infoResponse.notTraitors
    val teammates = infoResponse.teammates
    val players = traitors + notTraitors
    when (infoResponse.gameState) {
        GameState.MISSION -> {
            players.forEach{
                this.sendMsg(it.first, "Голосование закончилось успешно")
                this.sendMsg(it.first, "Миссия начинается")
            }
            teammates.forEach{
                this.sendMsg(it.first, "Решите исход миссии", Buttons.MISSION_BUTTONS)
            }
        }
        GameState.TEAMING -> {
            players.forEach{
                this.sendMsg(it.first, "Голосование решило поменять команду.")
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.sendMsg(
                leader.first, "Вы лидер. Набирайте команду",
                Buttons.getTeamingButtons(players.shuffled())
            )
        }
        GameState.END -> {
            players.forEach{
                this.sendMsg(it.first, "Игра закончилась")
            }
        }
    }
}

fun Bot.mission(infoResponse: InfoResponse){
    val traitors = infoResponse.traitors
    val leader = infoResponse.missionLeader
    val notTraitors = infoResponse.notTraitors
    val countSuccessMissions = infoResponse.countSuccessedMissions
    val countFailedMissions = infoResponse.countFailedMissions
    val players = traitors + notTraitors
    players.forEach{
        this.sendMsg(it.first, "Миссия завершилась")
        this.sendMsg(it.first, "Счет сопротивленцы $countSuccessMissions, предатели $countFailedMissions")
    }
    when(infoResponse.gameState){
        GameState.TEAMING -> {
            players.forEach{
                this.sendMsg(it.first, "Новый лидер: ${leader.second}")
            }
            this.sendMsg(
                leader.first, "Вы лидер. Набирайте команду",
                Buttons.getTeamingButtons(players.shuffled())
            )
        }
        GameState.END -> {
            players.forEach{
                this.sendMsg(it.first, "Игра закончилась")
            }
        }
    }
}