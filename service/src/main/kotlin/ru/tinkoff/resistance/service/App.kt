package ru.tinkoff.resistance.service

import AppConfig
import DataBaseConfig
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import ru.tinkoff.resistance.service.player.playerComponents
import ru.tinkoff.resistance.service.player.playerModule
import ru.tinkoff.resistance.service.plugin.configureSerialization

fun main() {
    val config = ConfigFactory.load().extract<AppConfig>()
    migrate(config.dataBase)
    val engine = embeddedServer(Netty, host = config.http.host, port = config.http.port) {
        di {
            coreComponents(config)
            playerComponents()
        }
        configureSerialization()
        playerModule()
    }
    engine.start()
}

fun DI.Builder.coreComponents(config: AppConfig) {
    bind<AppConfig>() with singleton { config }
    bind<Database>() with singleton {
        Database.connect(
            url = config.dataBase.url,
            user = config.dataBase.user,
            password = config.dataBase.password
        )
    }
}

fun migrate(dataBaseConfig: DataBaseConfig) {
    Flyway
        .configure()
        .dataSource(
            dataBaseConfig.url,
            dataBaseConfig.user,
            dataBaseConfig.password)
        .load()
        .migrate()
}