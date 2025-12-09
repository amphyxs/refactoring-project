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

CREATE OR REPLACE FUNCTION checkGameOnUniqueFunction() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM games WHERE name = NEW.name AND dev_login = NEW.dev_login) THEN
        RAISE EXCEPTION 'This game already exists for this developer';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION checkShopOnUniqueFunction() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM shop WHERE game_id = NEW.game_id) THEN
        RAISE EXCEPTION 'This game already exists in the shop';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION checkLibraryOnUniqueFunction() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM library WHERE user_login = NEW.user_login AND game_id = NEW.game_id) THEN
        RAISE EXCEPTION 'User already owns this game in the library';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION checkInventoryOnUniqueFunction() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM inventory WHERE user_login = NEW.user_login AND item_id = NEW.item_id AND id != NEW.id) THEN
        -- Update existing record instead of creating duplicate
        UPDATE inventory 
        SET amount = amount + NEW.amount 
        WHERE user_login = NEW.user_login AND item_id = NEW.item_id AND id != NEW.id;
        RETURN NULL; -- Prevent insert
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sellItemOnMarket() RETURNS TRIGGER AS $$
BEGIN
    -- Check if user has the item in inventory
    IF NOT EXISTS (SELECT 1 FROM inventory WHERE user_login = NEW.user_login AND item_id = NEW.item_id AND amount > 0) THEN
        RAISE EXCEPTION 'User does not own this item';
    END IF;
    
    -- Decrease item amount in inventory
    UPDATE inventory 
    SET amount = amount - 1 
    WHERE user_login = NEW.user_login AND item_id = NEW.item_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
