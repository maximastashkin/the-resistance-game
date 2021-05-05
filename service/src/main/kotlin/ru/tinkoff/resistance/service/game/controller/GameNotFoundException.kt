package ru.tinkoff.resistance.service.game.controller

import ru.tinkoff.resistance.errocodes.GameErrorCode

class GameNotFoundException(override val message: String, val errorCode: GameErrorCode) : Exception(message)