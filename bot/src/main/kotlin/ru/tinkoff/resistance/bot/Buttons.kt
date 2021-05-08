package ru.tinkoff.resistance.bot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

class Buttons {
    companion object {
        val START_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Создать", "create"),
            InlineKeyboardButton.CallbackData("Войти", "join"),
        )
        val VOTING_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("За", "voteYes"),
            InlineKeyboardButton.CallbackData("Против", "voteNo"),
        )
        val MISSION_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Успех", "voteSuccess"),
            InlineKeyboardButton.CallbackData("Фейл", "voteFail"),
        )
        val START_GAME = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Начать игру", "start")
            )
        val TEAMING_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Набрать команду", "teaming")
        )
        val TEST_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("voting", "voting"),
            InlineKeyboardButton.CallbackData("mission", "mission"),
            InlineKeyboardButton.CallbackData("start", "start"),
            InlineKeyboardButton.CallbackData("teaming", "teaming"),
        )

    }
}