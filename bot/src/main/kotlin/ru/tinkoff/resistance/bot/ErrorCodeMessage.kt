package ru.tinkoff.resistance.bot

import ru.tinkoff.resistance.errocodes.CommandErrorCode

fun CommandErrorCode.getMessage(): String =
    when(this){
        CommandErrorCode.ALREADY_IN_GAME -> {
            "Вы уже в игре"
        }
        CommandErrorCode.FULL_LOBBY -> {
            "В игре нет мест"
        }
        CommandErrorCode.NOT_HOST_START_GAME -> {
            "Только хост может начать игру"
        }
        CommandErrorCode.ALREADY_STARTED -> {
            "Игра уже началась"
        }
        CommandErrorCode.NOT_ENOUGH_PLAYERS_TO_START -> {
            "Недостаточно игроков, чтобы начать"
        }
        CommandErrorCode.ADD_PLAYER_IN_NOT_TEAM_STATE -> {
            "Добавленный игрок не в игре"
        }
        CommandErrorCode.NOT_LEADER_ADD_PLAYER_TO_TEAM -> {
            "Только лидер может добавлять игрока"
        }
        CommandErrorCode.ALREADY_IN_TEAM -> {
            "Игрок уже в команде"
        }
        CommandErrorCode.VOTE_IN_NOT_VOTE_STATE -> {
            "Голосовать можно только в процессе голосования"
        }
        CommandErrorCode.ALREADY_VOTE -> {
            "Вы уже проголосовали"
        }
        CommandErrorCode.DO_MISSION_IN_NOT_MISSION_STATE -> {
            "Выбирать исход миссии можно только в процессе миссии"
        }
        CommandErrorCode.PLAYER_DONT_DO_MISSION -> {
            "Вы не в миссии"
        }
        CommandErrorCode.ALREADY_DONE_MISSION -> {
            "Миссия уже завершилась"
        }
        CommandErrorCode.GAME_NOT_FOUND -> {
            "Игра не найдена"
        }
        CommandErrorCode.PLAYER_NOT_IN_GAME -> {
            "Игрок не в игре"
        }
        else -> {
            "Что-то пошло не так"
        }
    }