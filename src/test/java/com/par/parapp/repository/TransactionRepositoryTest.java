package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.Item;
import com.par.parapp.model.Game;
import com.par.parapp.model.User;
import com.par.parapp.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User testUser;
    private Item testItem;
    private Game testGame;

    @BeforeEach
    void setUp() {
        Wallet testWallet = new Wallet(500.0);
        testWallet = walletRepository.save(testWallet);

        testUser = new User(
            "transactiontest",
            "active",
            "password123",
            "transactiontest@example.com",
            LocalDate.now()
        );
        testUser.setWallet(testWallet);
        testUser = userRepository.save(testUser);

        testGame = new Game();
        testGame.setName("TestTransactionGame");
        testGame.setGameUrl("test-game-url");
        testGame = gameRepository.save(testGame);

        testItem = new Item();
        testItem.setName("Test Sword");
        testItem.setRarity("Обычная");
        testItem.setGame(testGame);
        testItem = itemRepository.save(testItem);
    }

    @Test
    void testCreateTransactionForItemPurchase_ShouldInsertTransaction() {
        int initialCount = transactionRepository.findAll().size();

        transactionRepository.createTransactionForItemPurchase(
            testUser.getLogin(),
            99.99,
            testItem.getId()
        );
        transactionRepository.flush();

        int finalCount = transactionRepository.findAll().size();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void testCreateMultipleTransactions_ShouldInsertAll() {
        int initialCount = transactionRepository.findAll().size();

        transactionRepository.createTransactionForItemPurchase(testUser.getLogin(), 10.0, testItem.getId());
        transactionRepository.createTransactionForItemPurchase(testUser.getLogin(), 20.0, testItem.getId());
        transactionRepository.createTransactionForItemPurchase(testUser.getLogin(), 30.0, testItem.getId());
        transactionRepository.flush();

        int finalCount = transactionRepository.findAll().size();
        assertThat(finalCount).isEqualTo(initialCount + 3);
    }

    @Test
    void testCreateTransactionWithDifferentAmounts_ShouldWork() {
        int initialCount = transactionRepository.findAll().size();
        Double[] amounts = {15.50, 99.99, 0.01, 1000.00};

        for (Double amount : amounts) {
            transactionRepository.createTransactionForItemPurchase(
                testUser.getLogin(),
                amount,
                testItem.getId()
            );
        }
        transactionRepository.flush();

        int finalCount = transactionRepository.findAll().size();
        assertThat(finalCount).isEqualTo(initialCount + amounts.length);
    }
}