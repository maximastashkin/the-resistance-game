package ru.rsreu

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() {
    val config = ConfigFactory.load().extract<AppConfig>()
    val client = HttpClient(CIO) {
        install(JsonFeature)
    }
    val engine = embeddedServer(Netty, port = config.http.port) {
        configureSerialization()
        val bot = botModule(config, client)
        bot.startWebhook()
        requestModule(bot)
    }
    engine.start(wait = true)
}
