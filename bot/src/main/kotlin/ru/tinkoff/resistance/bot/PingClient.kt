package ru.tinkoff.resistance.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import ru.tinkoff.resistance.bot.telegramBot.gameOver

import ru.tinkoff.resistance.bot.telegramBot.sendMsg
import ru.tinkoff.resistance.model.response.InfoResponse

fun Application.pingClient(config: AppConfig, client: HttpClient, bot: Bot) {
    val ticker = ticker(config.server.tickRate)

    launch {
        for (event in ticker) {
            val ids: List<Long> = client
                .get<HttpResponse>(config.server.url + "game/getallactiveusers")
                .receive()
            pingIds(ids, config, client, bot)
        }
    }
}

suspend fun pingIds(ids: List<Long>, config: AppConfig, client: HttpClient, bot: Bot) {
    ids.forEach {
        val message = bot.sendMsg(it, "Test connection", null)
        if (message.first?.isSuccessful != true) {
            val infoResponse =
                client.get<HttpResponse>(config.server.url + config.server.closeRoute + "/$it").receive<InfoResponse>()
            bot.gameOver(infoResponse, client, config)
        }
        runCatching {
            bot.deleteMessage(ChatId.fromId(it), message.first!!.body()!!.result!!.messageId)
        }
    }
}