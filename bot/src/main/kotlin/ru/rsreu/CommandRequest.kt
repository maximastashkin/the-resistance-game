package ru.rsreu

import kotlinx.serialization.Serializable

@Serializable
data class CommandRequest(val id: Long, val command: String, val args: List<String>)