package ru.tinkoff.resistance.service.player

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton
import ru.tinkoff.resistance.requests.models.player.PlayerCreateRequest

fun Application.playerModule() {
    val service: PlayerService by closestDI().instance()
    routing {
        route ("/player") {
            post {
                val request = call.receive<PlayerCreateRequest>()
                runCatching {
                    service.create(request.apiId, request.name, null)
                }.onSuccess {
                    call.respond(it)
                }.onFailure {
                    call.respond(HttpStatusCode.NotAcceptable)
                }
            }
        }
    }
}

fun DI.Builder.playerComponents() {
    bind<PlayerDao>() with singleton { PlayerDao(instance()) }
    bind<PlayerService>() with singleton {PlayerService(instance(), instance())}
}