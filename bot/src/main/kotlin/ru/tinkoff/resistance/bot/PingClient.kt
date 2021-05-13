package ru.tinkoff.resistance.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.dice.DiceEmoji
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch

import ru.tinkoff.resistance.bot.telegramBot.sendMsg

fun Application.pingClient(config: ServerConfig, client: HttpClient, bot: Bot) {
    val ticker = ticker(5000)

    launch {
        for (event in ticker) {
            val ids: List<Long> = client
                .get<HttpResponse>(config.url + "game/getallactiveusers")
                .receive()
            println(ids)
            val nonActive = mutableListOf<Long>()
            ids.forEach {
                val message = bot.sendMsg(it, "Test connection", null)
                if (message.second != null)
                    nonActive.add(it)
                else
                    bot.deleteMessage(ChatId.fromId(it), message.first!!.body()!!.result!!.messageId)
            }
            if (nonActive.isNotEmpty()) {
                throw InactivePlayerException(nonActive)
            }
        }
    }
}