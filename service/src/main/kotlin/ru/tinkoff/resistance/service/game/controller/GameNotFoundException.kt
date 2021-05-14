package ru.tinkoff.resistance.service.game.controller

import ru.tinkoff.resistance.errocodes.CommandErrorCode

class GameNotFoundException(override val message: String, val errorCode: CommandErrorCode) : Exception(message)