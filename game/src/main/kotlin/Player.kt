import kotlinx.serialization.Serializable

enum class Role(num: Int) {
    NONE(-1),
    RESISTANCE(0),
    TRAITOR(1)
}

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