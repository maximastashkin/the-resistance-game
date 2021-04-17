import kotlinx.serialization.Serializable

@Serializable
class Mission(val players: Int, var missionResult: MissionResult)