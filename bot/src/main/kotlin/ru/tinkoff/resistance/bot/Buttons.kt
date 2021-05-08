package ru.tinkoff.resistance.bot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

class Buttons {
    companion object {
        val START_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Create", "create"),
            InlineKeyboardButton.CallbackData("Join", "join"),
        )
        val VOTING_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Yes", "voteYes"),
            InlineKeyboardButton.CallbackData("No", "voteNo"),
        )
        val MISSION_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("Success", "voteSuccess"),
            InlineKeyboardButton.CallbackData("Fail", "voteFail"),
        )
        val START_GAME = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("start", "start")
            )
        val TEST_BUTTONS = InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.CallbackData("voting", "voting"),
            InlineKeyboardButton.CallbackData("mission", "mission"),
            InlineKeyboardButton.CallbackData("start", "start"),
            InlineKeyboardButton.CallbackData("teaming", "teaming"),
        )

    }
}