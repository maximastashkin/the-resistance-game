package ru.tinkoff.resistance.game.commands

import java.lang.Exception

class CommandExecutionException(override val message: String, val errorCode: CommandErrorCode): Exception(message)