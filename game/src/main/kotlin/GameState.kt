/**
 * Состояние игры
 * @property LOBBY - состояние, в котором подключаются игроки
 * @property START - состояние, которое пока не используется
 * @property TEAMING - состояние, в котором происходит набор игроков для миссии
 * @property VOTING - состояние, в котором голосуют за команду
 * @property MISSION - состояние, в котором голосуют за выполнение миссии
 * @property END - конец игры
 */
enum class GameState(num: Int) {
    LOBBY(0),
    START(1),
    TEAMING(2),
    VOTING(3),
    MISSION(4),
    END(5)
}