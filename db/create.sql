CREATE TABLE wallets
(
    id      SERIAL PRIMARY KEY,
    balance DOUBLE PRECISION DEFAULT 0
);

CREATE TABLE users
(
    login             VARCHAR(10) PRIMARY KEY,

    password          VARCHAR(255) NOT NULL,

    status            VARCHAR(32) DEFAULT 'Не в сети',
    last_login_date   DATE,
    email             VARCHAR(64)  NOT NULL,
    registration_date DATE         NOT NULL,
    wallet_id         INTEGER REFERENCES wallets ON DELETE CASCADE ON UPDATE CASCADE
);



CREATE TABLE games
(
    id               SERIAL PRIMARY KEY,
    name             VARCHAR(64)  NOT NULL,
    development_date DATE,
    game_url         VARCHAR(256) NOT NULL,
    dev_login        VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE shop
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

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_login VARCHAR(16) REFERENCES Users(login) ON DELETE SET NULL,
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


CREATE TABLE library
(
    id            SERIAL PRIMARY KEY,
    user_login    VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id       INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    last_run_date timestamp
);

CREATE TABLE items
(
    id       SERIAL PRIMARY KEY,
    game_id  INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    name     VARCHAR(20)  NOT NULL,
    rarity   VARCHAR(128) NOT NULL,
    item_url VARCHAR(256) NOT NULL
);

CREATE TABLE inventory
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    item_id    INTEGER REFERENCES items ON DELETE CASCADE ON UPDATE CASCADE,
    amount     INTEGER DEFAULT 0
);

CREATE TABLE user_activity
(
    id            SERIAL PRIMARY KEY,
    user_login    VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    activity_text VARCHAR(255),
    send_date     timestamp
);

CREATE TABLE guides
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id    INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    guide_text VARCHAR(1000) NOT NULL,
    send_date  timestamp     NOT NULL
);

CREATE TABLE reviews
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    game_id    INTEGER REFERENCES games ON DELETE CASCADE ON UPDATE CASCADE,
    review_text VARCHAR(1000) NOT NULL,
    send_date  timestamp     NOT NULL
);

CREATE TABLE market
(
    id         SERIAL PRIMARY KEY,
    user_login VARCHAR(10) REFERENCES users ON DELETE CASCADE ON UPDATE CASCADE,
    item_id    INTEGER REFERENCES items ON DELETE CASCADE ON UPDATE CASCADE,
    price      DOUBLE PRECISION NOT NULL
);



