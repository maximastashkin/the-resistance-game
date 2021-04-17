package commands

import Game

interface Command {
    val senderId: Int
    val senderName: String

    fun execute(game: Game)
}