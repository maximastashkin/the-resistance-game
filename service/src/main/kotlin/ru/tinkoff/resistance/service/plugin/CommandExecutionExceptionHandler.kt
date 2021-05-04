package ru.tinkoff.resistance.service.plugin

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import ru.tinkoff.resistance.game.commands.CommandExecutionException

fun Application.configureCommandExecutionExceptionHandler() {
    install(StatusPages) {
        exception<CommandExecutionException> {
            call.respond(HttpStatusCode.NotAcceptable, it.errorCode)
        }
    }
}