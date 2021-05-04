package ru.rsreu

import com.github.kotlintelegrambot.Bot
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.requestModule(bot: Bot){
    routing {
        post("/") {
            val receiveBody = call.receiveText()
            bot.processUpdate(receiveBody)
            call.respond(HttpStatusCode.OK)
        }
    }
}
