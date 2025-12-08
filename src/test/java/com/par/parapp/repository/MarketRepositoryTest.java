package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.*;
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
class MarketRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private MarketRepository marketRepository;

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

    private User seller;
    private Game game;
    private Item item1;
    private Item item2;
    private Market market1;
    private Market market2;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet(1000.0);
        wallet = walletRepository.save(wallet);

        seller = new User(
            "markettest",
            "active",
            "password123",
            "markettest@example.com",
            LocalDate.now()
        );
        seller.setWallet(wallet);
        seller = userRepository.save(seller);

        game = new Game();
        game.setName("Test Game");
        game.setGameUrl("https://example.com/testgame");
        game = gameRepository.save(game);

        item1 = new Item();
        item1.setName("Legendary Sword");
        item1.setRarity("Легендарная");
        item1.setGame(game);
        item1 = itemRepository.save(item1);

        item2 = new Item();
        item2.setName("Epic Shield");
        item2.setRarity("Эпическая");
        item2.setGame(game);
        item2 = itemRepository.save(item2);

        market1 = new Market();
        market1.setUser(seller);
        market1.setItem(item1);
        market1.setPrice(150.0);
        market1 = marketRepository.save(market1);

        market2 = new Market();
        market2.setUser(seller);
        market2.setItem(item2);
        market2.setPrice(75.0);
        market2 = marketRepository.save(market2);
    }

    @Test
    void testGetAllSlots_ShouldReturnAllMarketListings() {
        Optional<List<Market>> result = marketRepository.getAllSlots();

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(m -> m.getItem().getName())
            .containsExactlyInAnyOrder("Legendary Sword", "Epic Shield");
    }

    @Test
    void testGetMarketById_WithExistingId_ShouldReturnMarket() {
        Optional<Market> result = marketRepository.getMarketById(market1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getItem().getName()).isEqualTo("Legendary Sword");
        assertThat(result.get().getPrice()).isEqualTo(150.0);
    }

    @Test
    void testGetMarketById_WithNonExistentId_ShouldReturnEmpty() {
        Optional<Market> result = marketRepository.getMarketById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllFromMarketByItemNameFilter_WithPartialName_ShouldFindItems() {
        Optional<List<Market>> result = marketRepository.getAllFromMarketByItemNameFilter("sword");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getItem().getName()).contains("Sword");
    }

    @Test
    void testGetAllFromMarketByItemNameFilter_CaseInsensitive_ShouldFindItems() {
        Optional<List<Market>> result = marketRepository.getAllFromMarketByItemNameFilter("SHIELD");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getItem().getName()).isEqualTo("Epic Shield");
    }

    @Test
    void testGetAllFromMarketByItemNameFilter_WithEmptyString_ShouldReturnAll() {
        Optional<List<Market>> result = marketRepository.getAllFromMarketByItemNameFilter("");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    void testGetAllFromMarketByItemNameFilter_WithNonExistentItem_ShouldReturnEmpty() {
        Optional<List<Market>> result = marketRepository.getAllFromMarketByItemNameFilter("NonExistent");

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testDeleteMarketById_ShouldRemoveMarketListing() {
        Long marketId = market1.getId();

        marketRepository.deleteMarketById(marketId);
        marketRepository.flush();
        entityManager.clear();

        Optional<Market> found = marketRepository.findById(marketId);
        assertThat(found).isEmpty();

        Optional<List<Market>> remaining = marketRepository.getAllSlots();
        assertThat(remaining).isPresent();
        assertThat(remaining.get()).hasSize(1);
    }

    @Test
    void testSaveMarket_ShouldPersistMarketListing() {
        Item newItem = new Item();
        newItem.setName("Rare Helmet");
        newItem.setRarity("Редкая");
        newItem.setGame(game);
        newItem = itemRepository.save(newItem);

        Market newMarket = new Market();
        newMarket.setUser(seller);
        newMarket.setItem(newItem);
        newMarket.setPrice(50.0);

        Market saved = marketRepository.save(newMarket);
        entityManager.flush();
        entityManager.clear();

        Optional<Market> found = marketRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getItem().getName()).isEqualTo("Rare Helmet");
        assertThat(found.get().getPrice()).isEqualTo(50.0);
    }
}