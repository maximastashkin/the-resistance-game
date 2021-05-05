# service routing
## > **/game/create/{hostApiId} - GET**
### hostApiId - id хоста, создающего игру.
##### Возвращаемые статусы
-----------
* **OK(200)** - Лоби успешно создано. Также в ответе возвращается id созданной игры;
* **NotAcceptable(406)** - Игрок, создающий лобби, уже в игре;
* **BadRequest(400)** - Неправильный запрос;
* **NotFound(404)** - Игрок не найден по telegram id. Также в ответе возваращается telegram id игрока.

## > **/game/join - POST**
### body - класс JoinGameRequest
##### Возвращаемые статусы
-----------
* **OK(200)** - Игрок успешно присоединился к игре (лобби)
* **NotFound(404)** - Игрок не найден по telegram id. Также в ответе возваращается telegram id игрока;
* **NotAcceptable(406)** - Игрок уже в игре. Также в ответе возвращается enum *CommandErrorCode.ALREADY_IN_GAME*.

## > **/game/start/{hostApiId} - GET**
### hostApiId - id хоста, запускаюзего игру
##### Возвращаемые статусы
-----------
* **OK(200)** - Игра успешно запущена. Переход в стадию выбора игроков в команду лидером. Также возвращается ответ - TeamingInfoResponse вида:
```
    {
        "basicInfoResponse": {
            "notTraitorsApiIds": [
                1,
                3,
                5
            ],
            "traitorsApiIds": [
                2,
                4
            ],
            "countFailedMissions": 0,
            "countSuccessedMissions": 0
        },
        "missionLeaderApiId": 4
    }
```
* **BadRequest(400)** - Неправильный запрос;
* **NotFound(404)** - Игрок не найден по telegram id. Также в ответе возваращается telegram id игрока;
* **NotAcceptable(406)** - В ответе возваращается 2 типа ENUMов-ошибок (требуют безопасного приведения при десериализации): *CommandErrorCode.NOT_HOST_START_GAME*, *CommandErrorCode.ALREADY_STARTED*, *CommandErrorCode.NOT_ENOUGH_PLAYERS_TO_START*, *GameErrorCode.PLAYER_NOT_IN_GAME*, *GameErrorCode.GAME_NOT_FOUND*.

## > **/game/chooseplayerformission - POST**
### body - класс ChoosePlayerForMissionRequest
##### Возвращаемые статусы
-----------
* **OK(200)** - Игрок успешно выбран лидером в команду;
* **MultiStatus(207)** - Игрок на последнее место в команду выбран лидером успешно. Переход в стадию голосования за команду. Также возвращается ответ - VotingInfoResponse вида:
```
    {
        "playersApiIds": [
            1,
            2,
            3,
            4,
            5
        ],
        "teammatesApiIds": [
            4,
            1
        ]
    }
```
* **NotFound(404)** - Игрок не найден по telegram id. Также в ответе возваращается telegram id игрока;
* **NotAcceptable(406)** - В ответе возваращается 2 типа ENUMов-ошибок (требуют безопасного приведения при десериализации): *CommandErrorCode.ADD_PLAYER_IN_NOT_TEAM_STATE*, *CommandErrorCode.NOT_LEADER_ADD_PLAYER_TO_TEAM*, *CommandErrorCode.ALREADY_IN_TEAM*, *GameErrorCode.PLAYER_NOT_IN_GAME*, *GameErrorCode.GAME_NOT_FOUND*.

## > **/game/voteforteam - POST**
### body - класс VoteForTeamRequest
##### Возвращаемые статусы
--------
* **OK(200)** - Игрок успешно проголосовал за текущий состав команды;
* **MultiStatus(207)** - Последний голос отдан успешно. Команда не принята. Переход в стадию выбора игроков в команду лидером. Также возвращается ответ - TeamingInfoResponse;
* **Gone(410)** - Команда отклонена в 5 раз подряд. Переход в стадию окончания игры (победа предателей). Также возвращается ответ - EndGameResponse вида:
```
    {
        basicInfoResponse": {
                    "notTraitorsApiIds": [
                        1,
                        3,
                        5
                    ],
                    "traitorsApiIds": [
                        2,
                        4
                    ],
                    "countFailedMissions": 0,
                    "countSuccessedMissions": 0
                },
                winner: "TRAITOR"
    }
```
* **NotAcceptable(406)** - В ответе возваращается 2 типа ENUMов-ошибок (требуют безопасного приведения при десериализации): *CommandErrorCode.VOTE_IN_NOT_VOTE_STATE*, *CommandErrorCode.ALREADY_VOTE*, *GameErrorCode.PLAYER_NOT_IN_GAME*, *GameErrorCode.GAME_NOT_FOUND*.
## > **/game/missionaction - POST**
### body - класс MissionActionRequest
##### Возвращаемые статусы
------------
* **OK(200)** - Игрок успешно проголосовал за исход текущей миссии;
* **MultiStatus(207)** - Последний голос отдан успешно. Миссия завершена. Переход в стадию выбора игроков в команду лидером. Также возвращается ответ - TeamingInfoResponse;
* **Gone(410)** - Последний голос отдан успешно. Миссия завершена. Победа одной из команд. Переход в стадию окончания игры. Также возвращается ответ - EndGameResponse.
* **NotAcceptable(406)** - В ответе возваращается 2 типа ENUMов-ошибок (требуют безопасного приведения при десериализации): *CommandErrorCode.DO_MISSION_IN_NOT_MISSION_STATE*, *CommandErrorCode.PLAYER_DONT_DO_MISSION*, *CommandErrorCode.ALREADY_DONE_MISSION*, *GameErrorCode.PLAYER_NOT_IN_GAME*, *GameErrorCode.GAME_NOT_FOUND*.