CREATE OR REPLACE PROCEDURE user_login_update(login_input VARCHAR, new_status VARCHAR) AS $$
BEGIN
    UPDATE users SET status = new_status, last_login_date = CURRENT_DATE WHERE login = login_input;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_game_name(game_id INTEGER) RETURNS VARCHAR AS $$
DECLARE
    game_name VARCHAR;
BEGIN
    SELECT name INTO game_name FROM Games WHERE id = game_id;
    RETURN game_name;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE add_wallet_balance(user_login VARCHAR, amount DOUBLE PRECISION) AS $$
DECLARE
    walletid BIGINT;
BEGIN
    -- Поиск wallet_id по login
    SELECT wallet_id INTO walletid FROM Users WHERE login = user_login;

    -- Проверка на существование кошелька
    IF walletid IS NULL THEN
        RAISE EXCEPTION 'Wallet not found for login %', user_login;
    END IF;
    
    UPDATE Wallets SET balance = balance + amount WHERE id = walletid;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION library_entry_check() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM Library WHERE user_login = NEW.user_login AND game_id = NEW.game_id) THEN
        RAISE EXCEPTION 'User already owns this game in the library';
    END IF;


    -- Check if user has enough balance (Assuming price is available in shop table)
    IF (SELECT balance FROM Wallets WHERE id = (SELECT wallet_id FROM Users WHERE login = NEW.user_login)) < 
       (SELECT price FROM shop WHERE game_id = NEW.game_id) THEN
        RAISE EXCEPTION 'Insufficient funds';
    END IF;


    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER library_entry_trigger
BEFORE INSERT ON Library
FOR EACH ROW
EXECUTE FUNCTION library_entry_check();

CREATE OR REPLACE FUNCTION unique_game_in_store() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM shop WHERE game_id = NEW.game_id) THEN
        RAISE EXCEPTION 'This game already exists in the store';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_item_ownership_for_sale() RETURNS TRIGGER AS $$
DECLARE
    item_quantity INTEGER;
BEGIN
    SELECT amount INTO item_quantity FROM Inventory WHERE user_login = NEW.user_login AND item_id = NEW.item_id;
    
    IF item_quantity IS NULL OR item_quantity < 1 THEN
        RAISE EXCEPTION 'User does not own this item or has insufficient quantity';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_ownership_before_sale
BEFORE INSERT ON market
FOR EACH ROW
EXECUTE FUNCTION check_item_ownership_for_sale();

CREATE OR REPLACE FUNCTION add_balance_on_item_sale() RETURNS TRIGGER AS $$
DECLARE
    item_price double precision;
BEGIN
    UPDATE Wallets
    SET balance = balance + OLD.price
    WHERE id = (SELECT wallet_id FROM Users WHERE login = OLD.user_login);
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_wallet_balance_on_sale
AFTER DELETE ON market
FOR EACH ROW
EXECUTE FUNCTION add_balance_on_item_sale();

CREATE OR REPLACE PROCEDURE chargebalanceForSoldItem(arg_login varchar, arg_balance double precision, item_id bigint)
AS
$$
DECLARE
    wallet_id_to_change integer;
BEGIN

    IF (SELECT users.login FROM users WHERE users.login = arg_login) IS NOT NULL
    THEN
        wallet_id_to_change = (SELECT users.wallet_id FROM users WHERE users.login = arg_login);
        UPDATE wallets
        SET balance= ((SELECT balance
                       FROM wallets
                       WHERE wallets.id = wallet_id_to_change) - arg_balance)
        WHERE wallets.id = wallet_id_to_change;

        INSERT INTO transactions(user_login, payment_method, amount, transaction_date, transaction_status, item_id)
        VALUES (arg_login, 'balance', arg_balance, current_timestamp, 'success', item_id);
    ELSE
        RAISE EXCEPTION 'Данный пользователь не зарегистрирован';
    END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_last_played_date() RETURNS TRIGGER AS $$
BEGIN
    NEW.last_run_date := CURRENT_DATE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_last_played_date_trigger
BEFORE UPDATE OF last_run_date ON Library
FOR EACH ROW
EXECUTE FUNCTION update_last_played_date();
