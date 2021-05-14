package ru.tinkoff.resistance.bot.plugin

import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}
