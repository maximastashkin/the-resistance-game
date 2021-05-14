package ru.tinkoff.resistance.service.plugin

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import ru.tinkoff.resistance.game.commands.CommandExecutionException
import ru.tinkoff.resistance.service.game.controller.GameNotFoundException
import ru.tinkoff.resistance.service.player.PlayerNotFoundException

fun Application.configureExceptionHandler() {
    install(StatusPages) {
        exception<GameNotFoundException> {
            call.respond(HttpStatusCode.InternalServerError, it.errorCode)
        }
        exception<CommandExecutionException> {
            call.respond(HttpStatusCode.InternalServerError, it.errorCode)
        }
        exception<PlayerNotFoundException> {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}