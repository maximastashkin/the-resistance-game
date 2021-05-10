package ru.tinkoff.resistance.service
data class AppConfig(val http: HttpConfig, val dataBase: DataBaseConfig)

data class HttpConfig(val host: String, val port: Int)

data class DataBaseConfig(val url: String, val user: String, val password: String)