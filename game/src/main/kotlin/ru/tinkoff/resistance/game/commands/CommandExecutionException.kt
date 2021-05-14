package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.errocodes.CommandErrorCode
import java.lang.Exception

class CommandExecutionException(override val message: String, val errorCode: CommandErrorCode) : Exception(message)