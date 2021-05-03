CREATE TABLE games(
    id SERIAL PRIMARY KEY
);

CREATE TABLE players(
    id SERIAL PRIMARY KEY,
    name varchar(50) NOT NULL,
    api_id bigint,
    current_game_id integer,
    FOREIGN KEY(current_game_id) REFERENCES games(id));