package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InventoryRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Game game;
    private Item item1;
    private Item item2;
    private Item item3;
    private Inventory inventory1;
    private Inventory inventory2;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet(1000.0);
        wallet = walletRepository.save(wallet);

        testUser = new User(
            "inventorytest",
            "active",
            "password123",
            "inventorytest@example.com",
            LocalDate.now()
        );
        testUser.setWallet(wallet);
        testUser = userRepository.save(testUser);

        game = new Game();
        game.setName("Test RPG Game");
        game.setGameUrl("https://example.com/rpggame");
        game = gameRepository.save(game);

        item1 = new Item();
        item1.setName("Health Potion");
        item1.setRarity("Обычная");
        item1.setGame(game);
        item1 = itemRepository.save(item1);

        item2 = new Item();
        item2.setName("Mana Potion");
        item2.setRarity("Обычная");
        item2.setGame(game);
        item2 = itemRepository.save(item2);

        item3 = new Item();
        item3.setName("Legendary Sword");
        item3.setRarity("Легендарная");
        item3.setGame(game);
        item3 = itemRepository.save(item3);

        inventory1 = new Inventory();
        inventory1.setUser(testUser);
        inventory1.setItem(item1);
        inventory1.setAmount(10);
        inventory1 = inventoryRepository.save(inventory1);

        inventory2 = new Inventory();
        inventory2.setUser(testUser);
        inventory2.setItem(item2);
        inventory2.setAmount(5);
        inventory2 = inventoryRepository.save(inventory2);
    }

    @Test
    void testFindAllByUser_WithPagination_ShouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Optional<Page<Inventory>> result = inventoryRepository.findAllByUser(
            testUser.getLogin(),
            pageRequest
        );

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).hasSize(2);
        assertThat(result.get().getTotalElements()).isEqualTo(2);
        assertThat(result.get().getContent()).extracting(inv -> inv.getItem().getName())
            .containsExactlyInAnyOrder("Health Potion", "Mana Potion");
    }

    @Test
    void testFindAllByUser_WithPaginationFirstPage_ShouldReturnCorrectPage() {
        Inventory inventory3 = new Inventory();
        inventory3.setUser(testUser);
        inventory3.setItem(item3);
        inventory3.setAmount(1);
        inventoryRepository.save(inventory3);

        PageRequest pageRequest = PageRequest.of(0, 2);

        Optional<Page<Inventory>> result = inventoryRepository.findAllByUser(
            testUser.getLogin(),
            pageRequest
        );

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).hasSize(2);
        assertThat(result.get().getTotalElements()).isEqualTo(3);
        assertThat(result.get().getTotalPages()).isEqualTo(2);
    }

    @Test
    void testFindAllByUser_WithNonExistentUser_ShouldReturnEmptyPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Optional<Page<Inventory>> result = inventoryRepository.findAllByUser(
            "nonexistent",
            pageRequest
        );

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEmpty();
        assertThat(result.get().getTotalElements()).isEqualTo(0);
    }

    @Test
    void testSaveInventory_ShouldPersistInventory() {
        Inventory newInventory = new Inventory();
        newInventory.setUser(testUser);
        newInventory.setItem(item3);
        newInventory.setAmount(3);

        Inventory saved = inventoryRepository.save(newInventory);
        entityManager.flush();
        entityManager.clear();

        Optional<Inventory> found = inventoryRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getItem().getName()).isEqualTo("Legendary Sword");
        assertThat(found.get().getAmount()).isEqualTo(3);
    }

    @Test
    void testUpdateInventory_ShouldUpdateAmount() {
        inventory1.setAmount(20);

        inventoryRepository.save(inventory1);
        entityManager.flush();
        entityManager.clear();

        Optional<Inventory> found = inventoryRepository.findById(inventory1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(20);
    }

    @Test
    void testDeleteInventory_ShouldRemoveInventory() {
        Long inventoryId = inventory1.getId();

        inventoryRepository.delete(inventory1);
        inventoryRepository.flush();
        entityManager.clear();

        Optional<Inventory> found = inventoryRepository.findById(inventoryId);
        assertThat(found).isEmpty();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Optional<Page<Inventory>> remaining = inventoryRepository.findAllByUser(
            testUser.getLogin(),
            pageRequest
        );
        assertThat(remaining).isPresent();
        assertThat(remaining.get().getTotalElements()).isEqualTo(1);
    }
}