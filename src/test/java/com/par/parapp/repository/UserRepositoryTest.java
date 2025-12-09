package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.User;
import com.par.parapp.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(100.0);
        testWallet = walletRepository.save(testWallet);

        testUser = new User(
            "testuser",
            "inactive",
            "password123",
            "testuser@example.com",
            LocalDate.now()
        );
        testUser.setWallet(testWallet);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testLoginAsUser_ShouldUpdateStatusToActiveAndLastLoginDate() {
        assertThat(testUser.getStatus()).isEqualTo("inactive");

        userRepository.loginAsUser(testUser.getLogin());
        userRepository.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByLogin(testUser.getLogin()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo("active");
        assertThat(updatedUser.getLastLoginDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void testLogoutFromUser_ShouldUpdateStatusToInactiveAndLastLoginDate() {
        userRepository.loginAsUser(testUser.getLogin());
        userRepository.flush();
        entityManager.clear();

        userRepository.logoutFromUser(testUser.getLogin());
        userRepository.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByLogin(testUser.getLogin()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo("inactive");
        assertThat(updatedUser.getLastLoginDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void testReplenishBalance_ShouldIncreaseWalletBalance() {
        Double initialBalance = testWallet.getBalance();
        Double amountToAdd = 50.0;

        userRepository.replenishBalance(testUser.getLogin(), amountToAdd);
        userRepository.flush();
        entityManager.clear();

        Wallet updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualTo(initialBalance + amountToAdd);
    }

    @Test
    void testReplenishBalanceSeller_ShouldIncreaseWalletBalance() {
        Double initialBalance = testWallet.getBalance();
        Double amountToAdd = 75.0;

        userRepository.replenishBalanceSeller(testUser.getLogin(), amountToAdd);
        userRepository.flush();
        entityManager.clear();

        Wallet updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualTo(initialBalance + amountToAdd);
    }

    @Test
    void testChargeBalanceCustomer_ShouldDecreaseWalletBalance() {
        Double initialBalance = testWallet.getBalance();
        Double amountToCharge = 30.0;

        userRepository.chargeBalanceCustomer(testUser.getLogin(), amountToCharge);
        userRepository.flush();
        entityManager.clear();

        Wallet updatedWallet = walletRepository.findById(testWallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualTo(initialBalance - amountToCharge);
    }

    @Test
    void testGetBalance_ShouldReturnCurrentBalance() {
        Double balance = userRepository.getBalance(testUser.getLogin());

        assertThat(balance).isEqualTo(testWallet.getBalance());
    }

    @Test
    void testGetBonuses_ShouldReturnCurrentBonuses() {
        Double bonuses = userRepository.getBonuses(testUser.getLogin());

        assertThat(bonuses).isEqualTo(0.0);
    }

    @Test
    void testUpdateIsTutorialCompleted_ShouldUpdateFlag() {
        assertThat(testUser.getIsTutorialCompleted()).isFalse();

        userRepository.updateIsTutorialCompleted(testUser.getLogin(), true);
        userRepository.flush();
        entityManager.clear();

        User updatedUser = userRepository.findByLogin(testUser.getLogin()).orElseThrow();
        assertThat(updatedUser.getIsTutorialCompleted()).isTrue();
    }
}