/**
 * Класс для конфигурации игры
 * @property MAX_PLAYERS максимальное число игроков в игре
 * @property MIN_PLAYERS минимальное число игроков в игре
 * @property countPlayerWithRoles количество мирных и предателей для определенного количества игроков
 * @property countPlayersWithMissions количество игроков для определенной миссии при определенном количестве игроков
 */
class GameConfiguration {
    companion object {
        const val MAX_PLAYERS = 10
        const val MIN_PLAYERS = 5

        private val countPlayerWithRoles = mapOf(
            5 to Pair(3, 2),
            6 to Pair(4, 2),
            7 to Pair(5, 3),
            9 to Pair(6, 3),
            10 to Pair(6, 4))

        private val countPlayersWithMissions = mapOf(
            5 to listOf(2, 3, 2, 3, 3),
            6 to listOf(2, 3, 4, 3, 4),
            7 to listOf(2, 3, 3, 4, 4),
            8 to listOf(3, 4, 4, 5, 5),
            9 to listOf(3, 4, 4, 5, 5),
            10 to listOf(3, 4, 4, 5, 5),
        )

        fun getRoles(playerCount: Int) = countPlayerWithRoles[playerCount]
        fun getMissions(playerCount: Int) = countPlayersWithMissions[playerCount]
    }
}