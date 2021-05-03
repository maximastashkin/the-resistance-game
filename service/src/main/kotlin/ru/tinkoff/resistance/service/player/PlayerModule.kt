package ru.tinkoff.resistance.service.player

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.singleton

fun Application.playerModule() {
    val service: PlayerService by closestDI().instance()
    routing {
        route ("/player") {
            post {
                val request = call.receive<PlayerCreateRequest>()
                call.respond(service.create(request.apiId, request.name, null))
            }
        }
    }
}

fun DI.Builder.playerComponents() {
    bind<PlayerDao>() with singleton { PlayerDao(instance()) }
    bind<PlayerService>() with singleton {PlayerService(instance(), instance())}
}

@Serializable
data class PlayerCreateRequest(val apiId: Long, val name: String)