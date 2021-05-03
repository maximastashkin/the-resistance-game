package ru.tinkoff.resistance.game.commands

import ru.tinkoff.resistance.game.Game

/**
 * Интерфейс для команд
 * @property senderId идентификатор отправителя команды
 * @property senderName имя отправителя команды
 */
interface Command {
    val senderId: Int
    val senderName: String

    /**
     * Логика выполнения команды для игры
     * @param game состояние игры
     */
    fun execute(game: Game)
}