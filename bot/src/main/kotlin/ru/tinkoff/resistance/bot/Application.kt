package ru.tinkoff.resistance.bot

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.tinkoff.resistance.bot.plugin.configureExceptionHandler
import ru.tinkoff.resistance.bot.telegramBot.botModule


fun main() {
    val config = ConfigFactory.load().extract<AppConfig>()
    val client = HttpClient(CIO) {
        install(JsonFeature)
        expectSuccess = false
    }
    val engine = embeddedServer(Netty, port = config.http.port) {
        configureSerialization()
        val bot = botModule(config, client = client)
        configureExceptionHandler(config.server, client)
        bot.startWebhook()
        requestModule(bot)
//        pingClient(config.server, client, bot)
    }
    engine.start()
}
