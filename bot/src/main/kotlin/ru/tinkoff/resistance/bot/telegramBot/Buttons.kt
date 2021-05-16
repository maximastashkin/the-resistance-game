package ru.tinkoff.resistance.bot.telegramBot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

class Buttons {
    companion object {
        val START_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Создать", "create"),
            InlineKeyboardButton.CallbackData("Войти", "join"),
            InlineKeyboardButton.Url("Правила", "https://hobbygames.ru/download/rules/Soprotivlenie_2_izdanie_Rules.pdf")
        )

        val LOBBY_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Покинуть лобби", "leave")
        )

        val VOTING_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("За", "voteYes"),
            InlineKeyboardButton.CallbackData("Против", "voteNo"),
        )

        val MISSION_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Успех", "missionSuccess"),
            InlineKeyboardButton.CallbackData("Саботаж", "missionFail"),
        )

        val MISSION_FOR_NOT_TRAITORS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Успех", "missionSuccess")
        )

        val START_GAME = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Начать игру", "start"),
            InlineKeyboardButton.CallbackData("Покинуть лобби", "leave")
        )

        fun getTeamingButtons(players: List<Pair<Long, String>>): InlineKeyboardMarkup {
            val buttons = arrayListOf<List<InlineKeyboardButton>>()
            players.forEach {
                buttons.add(listOf(InlineKeyboardButton.CallbackData(it.second, "invite ${it.first}")))
            }
            return InlineKeyboardMarkup.create(buttons)
        }
    }
}