CREATE TABLE IF NOT EXISTS wallets
(
    id      SERIAL PRIMARY KEY,
    balance DOUBLE PRECISION DEFAULT 0,
    bonuses DOUBLE PRECISION DEFAULT 0
);

CREATE TABLE IF NOT EXISTS roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_DEV') ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS users
(
    login             VARCHAR(10) PRIMARY KEY,

    password          VARCHAR(255) NOT NULL,

    status            VARCHAR(32) DEFAULT 'Не в сети',
    last_login_date   DATE,
    email             VARCHAR(64)  NOT NULL,
    registration_date DATE         NOT NULL,
    is_tutorial_completed BOOLEAN DEFAULT FALSE,
    wallet_id         INTEGER REFERENCES wallets ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id VARCHAR(10) REFERENCES users(login) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS genres
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS games
(
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(64)  NOT NULL,
    development_date DATE,
    game_url         VARCHAR(256) NOT NULL,
    dev_login        VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS game_genres
(
    game_id  INTEGER REFERENCES games(id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (game_id, genre_id)
);

CREATE TABLE IF NOT EXISTS shop
(
    id                 SERIAL PRIMARY KEY,
    game_id            INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    price              DOUBLE PRECISION NOT NULL,
    description        TEXT         NOT NULL,
    picture_cover      VARCHAR(256) NOT NULL,
    picture_shop       VARCHAR(256) NOT NULL,
    picture_gameplay_1 VARCHAR(256) NOT NULL,
    picture_gameplay_2 VARCHAR(256) NOT NULL,
    picture_gameplay_3 VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS library
(
    id            SERIAL PRIMARY KEY,
    user_login    VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id       INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    last_run_date timestamp
);

CREATE TABLE IF NOT EXISTS items
(
    id       SERIAL PRIMARY KEY,
    game_id  INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    name     VARCHAR(20)  NOT NULL,
    rarity   VARCHAR(128) NOT NULL,
    item_url VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    user_login VARCHAR(16) REFERENCES users(login) ON DELETE SET NULL,
    payment_method VARCHAR(64),
    amount DOUBLE PRECISION,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_status VARCHAR(64),
    item_id INTEGER REFERENCES items(id) ON DELETE CASCADE DEFAULT NULL,
    game_id INTEGER REFERENCES games(id) ON DELETE CASCADE DEFAULT NULL,
    wallet_id INTEGER REFERENCES wallets(id) ON DELETE CASCADE DEFAULT NULL,
    CONSTRAINT check_item_game_wallet CHECK (
        (item_id IS NOT NULL AND game_id IS NULL AND wallet_id IS NULL) OR
        (item_id IS NULL AND game_id IS NOT NULL AND wallet_id IS NULL) OR
        (item_id IS NULL AND game_id IS NULL AND wallet_id IS NOT NULL)
    )
);

CREATE TABLE IF NOT EXISTS inventory
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    item_id    INTEGER REFERENCES items ON DELETE CASCADE ON UPDATE CASCADE,
    amount     INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_activity
(
    id            SERIAL PRIMARY KEY,
    user_login    VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    activity_text VARCHAR(255),
    send_date     timestamp
);

CREATE TABLE IF NOT EXISTS guides
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id    INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    guide_text VARCHAR(1000) NOT NULL,
    send_date  timestamp     NOT NULL
);

CREATE TABLE IF NOT EXISTS reviews
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id    INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    review_text VARCHAR(1000) NOT NULL,
    send_date  timestamp     NOT NULL
);

CREATE TABLE IF NOT EXISTS market
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    item_id    INTEGER REFERENCES items ON DELETE CASCADE ON UPDATE CASCADE,
    price      DOUBLE PRECISION NOT NULL
);

INSERT INTO genres (name) VALUES ('Action') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Adventure') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('RPG') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Strategy') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Simulation') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Casual') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Puzzle') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Sports') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Racing') ON CONFLICT DO NOTHING;
INSERT INTO genres (name) VALUES ('Shooter') ON CONFLICT DO NOTHING;
