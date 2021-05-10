CREATE TABLE games(
    id SERIAL PRIMARY KEY,
    host_id integer,
    date_time timestamp,
    winner integer
);

CREATE TABLE players(
    id SERIAL PRIMARY KEY,
    name varchar(50) NOT NULL,
    api_id bigint unique,
    current_game_id integer
);

CREATE TABLE gameshistory(
    player_id integer,
    game_id integer
)