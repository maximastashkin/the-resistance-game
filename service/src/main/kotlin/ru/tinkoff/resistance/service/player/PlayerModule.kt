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
import ru.tinkoff.resistance.model.request.PlayerCreateRequest

fun Application.playerModule() {
    val service: PlayerService by closestDI().instance()
    routing {
        route("/") {
            get {
                call.respond(HttpStatusCode.OK)
            }
        }
        route("/player") {
            post {
                val request = call.receive<PlayerCreateRequest>()
                runCatching {
                    service.create(request.apiId, request.name, -1)
                }.onSuccess {
                    call.respond(HttpStatusCode.Created)
                }.onFailure {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

fun DI.Builder.playerComponents() {
    bind<PlayerDao>() with singleton { PlayerDao(instance()) }
    bind<PlayerService>() with singleton { PlayerService(instance(), instance()) }
}