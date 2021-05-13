package ru.tinkoff.resistance.bot.plugin

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.features.*
import ru.tinkoff.resistance.bot.InactivePlayerException
import ru.tinkoff.resistance.bot.ServerConfig

fun Application.configureExceptionHandler(config:ServerConfig, client: HttpClient) {
    install(StatusPages) {
        exception<InactivePlayerException> {
            it.ids.forEach {
                client.delete(config.url + "game/close/$it")
            }
        }
    }
}