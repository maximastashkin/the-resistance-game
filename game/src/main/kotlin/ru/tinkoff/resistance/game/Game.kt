package ru.tinkoff.resistance.game

import ru.tinkoff.resistance.game.commands.Command
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.tinkoff.resistance.model.game.GameState
import ru.tinkoff.resistance.model.game.Role
import kotlin.properties.Delegates
import kotlin.random.Random

/**
 * Основной класс игры, который хранит состояние игры
 * @param id идентификатор игры
 * @param hostId идентификатор создателя сессии
 * @param hostName имя создателя сессии
 *
 * @property players список игроков в сессии
 * @property missions список миссия с количеством игроков для определенной миссии
 * @property winner роль, которая победила в сессии
 * @property gameState определенное состояние игры
 * @property onGameStateChanged событие, которое вызывается при изменении свойства GameState
 * @property missionLeader индентификатор лидера текущей миссии
 * @property currentMission номер текущей миссии
 * @property failedVotes количество неудавшихся наборов игроков для миссии
 * @property teammates список игроков, которые учавствуют в текущей миссии
 * @property votes список голосов за выбранную лидером команду
 *
 * @constructor добавляет в список игроков создателя сессии
 */
@Serializable
class Game(val id: Int, val hostId: Int, private val hostName: String) {

    var players: MutableList<Player> = mutableListOf()
    private var missions: List<Mission> = emptyList()

    var winner = Role.NONE

    var gameState: GameState by Delegates.observable(GameState.LOBBY) { _, old, new ->
        onGameStateChanged?.invoke(old, new)
    }

    @kotlinx.serialization.Transient
    var onGameStateChanged: ((GameState, GameState) -> Unit)? = null

    var missionLeader = -1
    var currentMission = -1

    var failedVotes = -1

    var teammates = mutableMapOf<Int, MissionResult>()
    private var votes = mutableMapOf<Int, VoteResult>()

    init {
        addPlayer(hostId, hostName)
    }

    /**
     * Проверяет является ли игрок создателем сессии
     * @param playerId идентификатор игрока для проверки
     * @return true - если игрок - создатель сессии, иначе false
     */
    fun isHost(playerId: Int): Boolean = hostId == playerId

    /**
     * Возвращает количество игроков в игре
     * @return количество игроков в игре
     */
    fun playerCount(): Int = players.size

    /**
     * Ищет игрока по его идентификатору
     * @param id идентификатор игрока для поиска
     * @return объект игрока, если найдено, иначе null
     */
    fun getPlayerById(id: Int): Player? = players.find { it.id == id }

    /**
     * Выполняет команду для текущей игры и возвращает json текущего состояния игры
     * @param command определенная команда, которая реализует интерфейс Command
     * @return json представление состояния игры
     */
    fun executeCommand(command: Command): String {
        command.execute(this)
        return Json.encodeToString(this)
    }

    /**
     * Устанавливает роль для игрока
     * @param player объект игрока
     * @param role роль RESISTANCE или TRAITOR
     */
    private fun setPlayerRole(player: Player, role: Role) {
        player.role = role
    }

    //LOBBY
    /**
     * Добавляет игрока в список
     * @param playerId идентификатор нового игрока
     * @param playerName имя нового игрока
     */
    fun addPlayer(playerId: Int, playerName: String) {
        players.add(Player(playerId, playerName))
    }

    //START
    /**
     * Запускает игру: раздает роли, устанавливает миссии, выбирает текущего лидера, запускает набор игроков
     */
    fun startGame() {
        missions = GameConfiguration.getMissions(playerCount())!!.map {
            Mission(it, MissionResult.NONE)
        }
        val roles = GameConfiguration.getRoles(playerCount())
        players.shuffled().mapIndexed() {  index, player ->
            if (index < roles!!.first) {
                setPlayerRole(player, Role.RESISTANCE)
            } else {
                setPlayerRole(player, Role.TRAITOR)
            }
        }

        votes = players.associate { it.id to VoteResult.UNVOTED } as MutableMap<Int, VoteResult>
        missionLeader = Random.nextInt(playerCount())
        gameState = GameState.START
        startNewTeaming()
    }

    //TEAMING
    /**
     * Добавляет в список участников миссии нового игрока и запускает голосование за выбранную команду
     * @param playerId идентификатор игрока для миссии
     */
    fun addPlayerToMission(playerId: Int) {
        teammates[playerId] = MissionResult.NONE
        if (teammates.size == getCountOfPlayerToCurrentMission()) {
            startVoting()
        }
    }

    /**
     * Проверяет учавствует ли игрок в миссии или нет
     * @param id идентификатор игрока для проверки
     * @return true - если игрок учавствует в миссии, иначе false
     */
    fun isContainsTeammate(id: Int): Boolean = teammates.contains(id)

    /**
     * Возвращает идентификатор лидера миссии
     * @return идентификатор лидера миссии
     */
    fun getLeaderId(): Int = players[missionLeader].id

    /**
     * Получает количество игроков для текущей миссии
     * @return количество игроков для текущей миссии
     */
    private fun getCountOfPlayerToCurrentMission(): Int = missions[currentMission].players

    /**
     * Запускает состояние набора игроков для миссии
     */
    private fun startNewTeaming() {
        failedVotes = -1
        currentMission++
        newTeaming()
    }

    /**
     * Перемещает указатель на следующего игрока для объявления его новым лидером миссии
     */
    private fun nextLeaderId() {
        if (missionLeader + 1 < playerCount()) {
            missionLeader++
        } else {
            missionLeader = 0
        }
    }

    /**
     * Создает новый набор игроков для миссии
     */
    private fun newTeaming() {
        nextLeaderId()
        failedVotes++
        gameState = GameState.TEAMING
        teammates = mutableMapOf()
        teammates[players[missionLeader].id] = MissionResult.NONE

        if (failedVotes == 5) {
            gameState = GameState.END
            winner = Role.TRAITOR
        }
    }

    //VOTING
    /**
     * Устанавливает голос определенного игрока в голосовании для набора команды
     * @param playerId номер, проголосовавшего игрока
     * @param vote состояние голоса (true - согласен, false - не согласен)
     */
    fun setTeamVote(playerId: Int, vote: Boolean) {
        val voteResult = if (vote) VoteResult.AGREE else VoteResult.DISAGREE

        votes[playerId] = voteResult

        if (isAllVoted()) {
            if (getCountDisagreeVotes() >= getCountAgreeVotes()) {
                newTeaming()
            } else {
                startMissionVote()
            }
        }
    }

    /**
     * Проверяет проголосовал игрок за команду или нет
     * @param id - идентификатор игрока
     * @return true - если игрок проголосовал, иначе false
     */
    fun isVoteToTeam(id: Int): Boolean = votes[id] != VoteResult.UNVOTED

    /**
     * Проверяет проголосовали ли все игроки или нет
     * @return true - если все игроки проголосовали, иначе false
     */
    private fun isAllVoted(): Boolean = votes.all { it.value != VoteResult.UNVOTED }

    /**
     * Запускает голосование
     */
    private fun startVoting() {
        gameState = GameState.VOTING
        setAllVotesToUnvoted()
    }

    /**
     * Устанавливает словарь (playerID - ru.tinkoff.resistanse.game.VoteResult) в исходное состояние (никто не проголосовал)
     */
    private fun setAllVotesToUnvoted() {
        for (key in votes.keys) {
            votes[key] = VoteResult.UNVOTED
        }
    }

    /**
     * @return возвращает количество игроков, которые согласились с командой
     */
    private fun getCountAgreeVotes(): Int = votes.count { it.value == VoteResult.AGREE }

    /**
     * @return возврашает количество игроков, которые не согласились с командой
     */
    private fun getCountDisagreeVotes(): Int = votes.count { it.value == VoteResult.DISAGREE }

    //MISSION
    /**
     * Устанавливает голос участника миссии за исход миссии
     * @param playerId идентификатор игрока
     * @param vote состояние голоса (true - согласен, false - не согласен)
     */
    fun setMissionVote(playerId: Int, vote: Boolean) {
        val voteResult = if (vote) MissionResult.SUCCESS else MissionResult.FAIL

        teammates[playerId] = voteResult

        if (isAllDoneMission()) {
            missions[currentMission].missionResult =
                if (getCountFailMissions() > 0) MissionResult.FAIL else MissionResult.SUCCESS
            if (getCountFailedMissions() == 3) {
                gameState = GameState.END
                winner = Role.TRAITOR
            } else if (getCountSuccessedMissions() == 3) {
                gameState = GameState.END
                winner = Role.RESISTANCE
            }

            if (gameState != GameState.END) {
                startNewTeaming()
            }
        }
    }

    /**
     * Проверяет учавствует игрок в миссии или нет
     * @param id идентификатор игрока
     * @return true, если игрок учавствует в миссии, иначе false
     */
    fun isInTeam(id: Int): Boolean = teammates[id] != null

    /**
     * Проверяет проголосовал ли игрок в миссии или нет
     * @param id идентификатор игрока
     * @return true, если игрок проголосовал, иначе false
     */
    fun doneMission(id: Int): Boolean = teammates[id]!! != MissionResult.NONE

    /**
     * Считает количество саботажных голосов
     * @return количество саботажных голосов
     */
    private fun getCountFailMissions(): Int = teammates.count { it.value == MissionResult.FAIL }

    /**
     * Запускает голосование для миссии
     */
    private fun startMissionVote() {
        gameState = GameState.MISSION
    }

    /**
     * Проверяет все ли проголосовали в миссиии ли нет
     * @return true, если все проголосовали, иначе false
     */
    private fun isAllDoneMission(): Boolean = teammates.all { it.value != MissionResult.NONE }

    /**
     * Считает количество выигранных миссий предателями
     * @return количество миссий, которые выиграли предатели
     */
     fun getCountFailedMissions(): Int = missions.count { it.missionResult == MissionResult.FAIL }

    /**
     * Считает количество выигранных миссий сопротивлением
     * @return количество миссий, которые выиграли сопротивление
     */
    fun getCountSuccessedMissions(): Int = missions.count { it.missionResult == MissionResult.SUCCESS }

    /**
     * @return список игроков-не предателей.
     */
    fun getNotTraitors(): List<Player> = players.filter { !it.isTraitor() }

    //FOR TESTS
    fun getTraitors(): List<Player> = players.filter { it.isTraitor() }
    fun getLeader(): Player = players[missionLeader]
    fun getPlayersToMission(): List<Player> = players.filter { it.id != getLeaderId() }.subList(0, missions[currentMission].players - 1)
    fun getPlayersExcludeLeader(): List<Player> = players.filter { it.id != getLeaderId() }
    fun getLastPlayer(): Player = players.last { it.id != getLeaderId() }
}