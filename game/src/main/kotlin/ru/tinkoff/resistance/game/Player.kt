package ru.tinkoff.resistance.game

import kotlinx.serialization.Serializable
import ru.tinkoff.resistance.model.role.Role

@Serializable
class Player(
    val id: Int,
    var name: String,
) {
    var role: Role = Role.NONE

    fun isResistance(): Boolean = role == Role.RESISTANCE
    fun isTraitor(): Boolean = role == Role.TRAITOR
    fun changeName(name: String) {
        this.name = name
    }
}