package ru.tinkoff.resistance.game

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() {
    val game = Game(5, 1, "KAKK")
    game.addPlayer(2, "TA")
    game.addPlayer(3, "DAGAG")
    game.addPlayer(4, "DAFGA")
    game.addPlayer(5, "DAGAGg")
    game.startGame()

    val string = Json.encodeToString(game)
    println(string)
}