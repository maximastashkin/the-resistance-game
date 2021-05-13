package ru.tinkoff.resistance.bot

data class AppConfig(
    val http: HttpConfig,
    val telegram: TelegramConfig,
    val server: ServerConfig
)

data class HttpConfig(val port: Int)

data class TelegramConfig(
    val token: String,
    val webhookUrl: String
)

data class ServerConfig(
    val url: String,
    val startRoute: String,
    val createRoute: String,
    val joinRoute: String,
    val leaveRoute: String,
    val closeRoute: String,
    val gameStartRoute: String,
    val choosePlayerForMission: String,
    val voteForTeam: String,
    val missionAction: String,
    val tickRate: Long
)