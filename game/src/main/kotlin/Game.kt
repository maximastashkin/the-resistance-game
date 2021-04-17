import commands.Command
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
class Game(private val id: Int,private val hostId: Int,private val hostName: String) {

    private var players: MutableList<Player> = mutableListOf()
    private var missions: List<Mission> = emptyList()

    private var winner = Role.NONE

    var gameState: GameState = GameState.LOBBY
    fun playerCount(): Int = players.size

    var missionLeader = -1
    var currentMission = -1

    private var teammates = mutableMapOf<Int, MissionResult>()
    private var votes = mutableMapOf<Int, VoteResult>()

    init {
        addPlayer(hostId, hostName)
    }

    fun isHost(playerId: Int): Boolean = hostId == playerId

    fun getPlayerById(id: Int): Player? = players.find { it.id == id }

    fun executeCommand(command: Command): String {
        command.execute(this)
        return Json.encodeToString(this)
    }

    private fun setPlayerRole(player: Player, role: Role) {
        player.role = role
    }

    //LOBBY
    fun addPlayer(playerId: Int, playerName: String) {
        players.add(Player(playerId, playerName))
    }

    //START
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

        votes = players.map { it.id to VoteResult.UNVOTED}.toMap() as MutableMap<Int, VoteResult>
        missionLeader = Random.nextInt(playerCount())
        gameState = GameState.START
        startNewMission()
    }

    //TEAMING
    fun addPlayerToMission(playerId: Int) {
        teammates[playerId] = MissionResult.NONE

        if (teammates.size == getCountOfPlayerToCurrentMission()) {
            startVoting()
        }
    }

    fun isContainsTeammate(id: Int): Boolean = teammates.contains(id)

    private fun getCountOfPlayerToCurrentMission(): Int = missions[currentMission].players

    private fun startNewMission() {
        currentMission++
        nextLeader()
    }

    private fun nextLeaderId() {
        if (missionLeader + 1 < playerCount()) {
            missionLeader++
        } else {
            missionLeader = 0
        }
    }

    private fun nextLeader() {
        nextLeaderId()
        gameState = GameState.TEAMING
        teammates = mutableMapOf()
        teammates[missionLeader] = MissionResult.NONE
    }

    //VOTING
    fun setVote(playerId: Int, vote: Boolean) {
        val voteResult = if (vote) VoteResult.AGREE else VoteResult.DISAGREE

        votes[playerId] = voteResult

        if (isAllVoted()) {
            if (getCountDisagreeVotes() >= getCountAgreeVotes()) {
                nextLeader()
            } else {
                startMissionVote()
            }
        }
    }

    fun isVoteToTeam(id: Int): Boolean = votes[id] != VoteResult.UNVOTED

    private fun isAllVoted(): Boolean = votes.all { it.value != VoteResult.UNVOTED }

    private fun startVoting() {
        gameState = GameState.VOTING
        setAllVotesToUnvoted()
    }

    private fun setAllVotesToUnvoted() {
        for (key in votes.keys) {
            votes[key] = VoteResult.DISAGREE
        }
    }

    private fun getCountAgreeVotes(): Int = votes.count { it.value == VoteResult.AGREE }

    private fun getCountDisagreeVotes(): Int = votes.count { it.value == VoteResult.DISAGREE }

    //MISSION
    fun setMissionVote(playerId: Int, vote: Boolean) {
        val voteResult = if (vote) MissionResult.SUCCESS else MissionResult.FAIL

        teammates[playerId] = voteResult

        if (isAllDoneMission()) {
            missions[currentMission].missionResult =
                if (getCountFailMissions() > 0) MissionResult.FAIL else MissionResult.SUCCESS
            if (getCountFailedMissions() == 3) {
                gameState = GameState.END
                winner = Role.TRAITOR
            }else if (getCountSuccessedMissions() == 3) {
                gameState = GameState.END
                winner = Role.RESISTANCE
            }
        }
    }

    fun isInTeam(id: Int): Boolean = teammates[id] != null

    fun doneMission(id: Int): Boolean = teammates[id]!! != MissionResult.NONE

    private fun getCountFailMissions(): Int = teammates.count { it.value == MissionResult.FAIL }

    private fun startMissionVote() {
        gameState = GameState.MISSION
    }

    private fun isAllDoneMission(): Boolean = teammates.all { it.value != MissionResult.NONE }

    private fun getCountFailedMissions(): Int = missions.count { it.missionResult == MissionResult.FAIL }

    private fun getCountSuccessedMissions(): Int = missions.count { it.missionResult == MissionResult.SUCCESS }

}