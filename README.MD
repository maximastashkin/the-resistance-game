# service routing
## > **/game/create - POST**
### body - класс CreateGameRequest
##### Возвращаемые статусы
-----------
* **Created(201)** - Лобби успешно создано. Также в ответе возвращается id созданной игры;
* **InternalServerError(500)** - Игрок, создающий лобби, уже в игре. Код ошибки: *CommandErrorCode.ALREADY_IN_GAME*;
* **NotFound(404)** - Игрок не найден по telegram id;

## > **/game/join - POST**
### body - класс JoinGameRequest
##### Возвращаемые статусы
-----------
* **OK(200)** - Игрок успешно присоединился к игре (лобби)
* **NotFound(404)** - Игрок не найден по telegram id;
* **InternalServerError(500)** - Игрок уже в игре. Также в ответе возвращается enum *CommandErrorCode.ALREADY_IN_GAME*.

## > **/game/start/{hostApiId} - GET**
### hostApiId - id хоста, запускающего игру
##### Возвращаемые статусы
-----------
* **OK(200)** - Игра успешно запущена. Переход в стадию выбора игроков в команду лидером. Также возвращается ответ - InfoResponse вида:
```
    {
        "gameState": "TEAMING",
        "notTraitors": [
            {
                "first": 1,
                "second": "test"
            },
            {
                "first": 3,
                "second": "test"
            },
            {
                "first": 4,
                "second": "test"
            }
        ],
        "traitors": [
            {
                "first": 2,
                "second": "test"
            },
            {
                "first": 5,
                "second": "test"
            }
        ],
        "teammates": [
            {
                "first": 5,
                "second": "test"
            }
        ],
        "countFailedMissions": 0,
        "countSuccessedMissions": 0,
        "missionLeader": {
            "first": 5,
            "second": "test"
        }
    }
```
* **BadRequest(400)** - Неправильный запрос;
* **NotFound(404)** - Игрок не найден по telegram id;
* **InternalServerError(500)** - Коды ошибок: *CommandErrorCode.NOT_HOST_START_GAME*, *CommandErrorCode.ALREADY_STARTED*, *CommandErrorCode.NOT_ENOUGH_PLAYERS_TO_START*, *CommandErrorCode.PLAYER_NOT_IN_GAME*, *CommandErrorCode.GAME_NOT_FOUND*.

## > **/game/chooseplayerformission - POST**
### body - класс ChoosePlayerForMissionRequest
##### Возвращаемые статусы
-----------
* **OK(200)** - Игрок успешно выбран лидером в команду. Также возвращается ответ - InfoResponse (Следует следить за изменениями GameState - признак перехода в другую стадию игры);
* **NotFound(404)** - Игрок не найден по telegram id;
* **InternalServerError(500)** - Коды ошибок: *CommandErrorCode.ADD_PLAYER_IN_NOT_TEAM_STATE*, *CommandErrorCode.NOT_LEADER_ADD_PLAYER_TO_TEAM*, *CommandErrorCode.ALREADY_IN_TEAM*, *CommandErrorCode.PLAYER_NOT_IN_GAME*, *CommandErrorCode.GAME_NOT_FOUND*.

## > **/game/voteforteam - POST**
### body - класс VoteForTeamRequest
##### Возвращаемые статусы
--------
* **OK(200)** - Игрок успешно проголосовал за текущий состав команды. Также возвращается ответ - InfoResponse (Следует следить за изменениями GameState - признак перехода в другую стадию игры);
* **InternalServerError(500)** - Коды ошибок: *CommandErrorCode.VOTE_IN_NOT_VOTE_STATE*, *CommandErrorCode.ALREADY_VOTE*, *CommandErrorCode.PLAYER_NOT_IN_GAME*, *CommandErrorCode.GAME_NOT_FOUND*.
* **NotFound(404)** - Игрок не найден по telegram id;
## > **/game/missionaction - POST**
### body - класс MissionActionRequest
##### Возвращаемые статусы
------------
* **OK(200)** - Игрок успешно проголосовал за исход текущей миссии. Также возвращается ответ - InfoResponse (Следует следить за изменениями GameState - признак перехода в другую стадию игры);
* **InternalServerError(500)** - ВКоды ошибок: *CommandErrorCode.DO_MISSION_IN_NOT_MISSION_STATE*, *CommandErrorCode.PLAYER_DONT_DO_MISSION*, *CommandErrorCode.ALREADY_DONE_MISSION*, *CommandErrorCode.PLAYER_NOT_IN_GAME*, *CommandErrorCode.GAME_NOT_FOUND*.
* **NotFound(404)** - Игрок не найден по telegram id;
## > **/player - POST**
### body - класс PlayerCreateRequest
##### Возвращаемые статусы
------------
* **Created(201)** - Игрок успешно создан;
* **InternalServerError(500)** - Игрок с таким apiId уже был создан, информация о нем хранится в бд.
* **NotFound(404)** - Игрок не найден по telegram id;