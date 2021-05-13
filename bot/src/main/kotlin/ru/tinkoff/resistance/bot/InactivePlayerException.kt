package ru.tinkoff.resistance.bot

class InactivePlayerException(val ids: List<Long>) : Exception()