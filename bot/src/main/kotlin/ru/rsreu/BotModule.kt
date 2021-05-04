package ru.rsreu

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.webhook
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

fun botModule(config: AppConfig, client: HttpClient): Bot {
    return bot {
        token = config.telegram.token

        webhook {
            url = config.telegram.webhookUrl
        }

        dispatch {
            command("test") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "test",
                    replyMarkup = Buttons.TEST_BUTTONS
                )
            }

            command("start") {
                val response: HttpResponse
                runBlocking {
                    response = client.post("http://localhost:8080/player") {
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        body = PlayerCreateRequest(message.chat.id, message.from!!.firstName)
                    }
                }
                if(response.status == HttpStatusCode.OK){
                    this.bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "Welcome. Create lobby or join an existing lobby?",
                        replyMarkup = Buttons.START_BUTTONS
                    )
                } else {
                    this.bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "Repeat? /start"
                    )
                }
            }

            callbackQuery("create") {
                val id = callbackQuery.from.id
                this.bot.sendMessage(
                    chatId = ChatId.fromId(id),
                    text = "Creating game...",
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("join") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "Enter chat id"
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("start") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "Start game..."
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("teaming"){
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "You are captain! Select team"
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("voting") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "Do you agree with such a team: ....",
                    replyMarkup = Buttons.VOTING_BUTTONS
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("voteYes") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "YES"
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("voteNo") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "NO"
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("mission"){
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "Choose the outcome of the mission",
                    replyMarkup = Buttons.MISSION_BUTTONS
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("voteSuccess") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "SUCCESS"
                )
                deleteMessage(callbackQuery, this.bot)
            }

            callbackQuery("voteFail") {
                this.bot.sendMessage(
                    chatId = ChatId.fromId(callbackQuery.from.id),
                    text = "FAIL"
                )
                deleteMessage(callbackQuery, this.bot)
            }
        }
    }
}

fun deleteMessage(callbackQuery: CallbackQuery, bot: Bot){
    bot.deleteMessage(
        chatId = ChatId.fromId(callbackQuery.from.id),
        messageId = callbackQuery.message!!.messageId
    )
}

@Serializable
data class PlayerCreateRequest(val apiId: Long, val name: String)