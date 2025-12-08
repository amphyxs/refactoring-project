package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.Game;
import com.par.parapp.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    private Game game1;
    private Game game2;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        game1 = new Game();
        game1.setName("RPG Adventure");
        game1.setGameUrl("https://example.com/rpg");
        game1 = gameRepository.save(game1);

        game2 = new Game();
        game2.setName("Action Shooter");
        game2.setGameUrl("https://example.com/shooter");
        game2 = gameRepository.save(game2);

        item1 = new Item();
        item1.setName("Health Potion");
        item1.setRarity("Обычная");
        item1.setGame(game1);
        item1 = itemRepository.save(item1);

        item2 = new Item();
        item2.setName("Health Potion");
        item2.setRarity("Редкая");
        item2.setGame(game1);
        item2 = itemRepository.save(item2);

        item3 = new Item();
        item3.setName("Legendary Sword");
        item3.setRarity("Легендарная");
        item3.setGame(game1);
        item3 = itemRepository.save(item3);
    }

    @Test
    void testExistsItemByGameAndNameAndRarity_WithExistingItem_ShouldReturnItem() {
        Optional<Item> result = itemRepository.existsItemByGameAndNameAndRarity(
            game1.getId(),
            "Health Potion",
            "Обычная"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Health Potion");
        assertThat(result.get().getRarity()).isEqualTo("Обычная");
    }

    @Test
    void testExistsItemByGameAndNameAndRarity_WithDifferentRarity_ShouldReturnCorrectItem() {
        Optional<Item> result = itemRepository.existsItemByGameAndNameAndRarity(
            game1.getId(),
            "Health Potion",
            "Редкая"
        );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Health Potion");
        assertThat(result.get().getRarity()).isEqualTo("Редкая");
    }

    @Test
    void testExistsItemByGameAndNameAndRarity_WithNonExistentItem_ShouldReturnEmpty() {
        Optional<Item> result = itemRepository.existsItemByGameAndNameAndRarity(
            game1.getId(),
            "Non Existent Item",
            "Обычная"
        );

        assertThat(result).isEmpty();
    }

    @Test
    void testExistsItemByGameAndNameAndRarity_WithWrongGame_ShouldReturnEmpty() {
        Optional<Item> result = itemRepository.existsItemByGameAndNameAndRarity(
            game2.getId(),
            "Health Potion",
            "Обычная"
        );

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllItemsByGameId_WithExistingGame_ShouldReturnAllItems() {
        Optional<List<Item>> result = itemRepository.getAllItemsByGameId(game1.getId());

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(3);
        assertThat(result.get()).extracting(Item::getName)
            .containsExactlyInAnyOrder("Health Potion", "Health Potion", "Legendary Sword");
    }

    @Test
    void testGetAllItemsByGameId_WithNonExistentGame_ShouldReturnEmpty() {
        Optional<List<Item>> result = itemRepository.getAllItemsByGameId(99999L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testGetAllItemsByGameId_WithGameWithoutItems_ShouldReturnEmpty() {
        Optional<List<Item>> result = itemRepository.getAllItemsByGameId(game2.getId());

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testSaveItem_ShouldPersistItem() {
        Item newItem = new Item();
        newItem.setName("Mana Potion");
        newItem.setRarity("Эпическая");
        newItem.setGame(game1);

        Item saved = itemRepository.save(newItem);
        entityManager.flush();
        entityManager.clear();

        Optional<Item> found = itemRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mana Potion");
        assertThat(found.get().getRarity()).isEqualTo("Эпическая");
    }

    @Test
    void testDeleteItem_ShouldRemoveItem() {
        Long itemId = item1.getId();

        itemRepository.delete(item1);
        itemRepository.flush();
        entityManager.clear();

        Optional<Item> found = itemRepository.findById(itemId);
        assertThat(found).isEmpty();

        Optional<List<Item>> remaining = itemRepository.getAllItemsByGameId(game1.getId());
        assertThat(remaining).isPresent();
        assertThat(remaining.get()).hasSize(2);
    }

    @Test
    void testFindAll_ShouldReturnAllItems() {
        List<Item> allItems = itemRepository.findAll();

        assertThat(allItems).hasSize(3);
    }

    @Test
    void testAddItemToGame2_ShouldIncreaseGame2ItemCount() {
        Item newItem = new Item();
        newItem.setName("Ammo Pack");
        newItem.setRarity("Обычная");
        newItem.setGame(game2);

        itemRepository.save(newItem);
        entityManager.flush();

        Optional<List<Item>> game2Items = itemRepository.getAllItemsByGameId(game2.getId());
        assertThat(game2Items).isPresent();
        assertThat(game2Items.get()).hasSize(1);
        assertThat(game2Items.get().get(0).getName()).isEqualTo("Ammo Pack");
    }
}