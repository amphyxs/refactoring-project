package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.Game;
import com.par.parapp.model.Shop;
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
class ShopRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    private Game game1;
    private Game game2;
    private Game game3;
    private Shop shop1;
    private Shop shop2;

    @BeforeEach
    void setUp() {
        game1 = new Game();
        game1.setName("The Witcher 3: Wild Hunt");
        game1.setGameUrl("https://example.com/witcher3");
        game1 = gameRepository.save(game1);

        game2 = new Game();
        game2.setName("Cyberpunk 2077");
        game2.setGameUrl("https://example.com/cyberpunk");
        game2 = gameRepository.save(game2);

        game3 = new Game();
        game3.setName("Elden Ring");
        game3.setGameUrl("https://example.com/eldenring");
        game3 = gameRepository.save(game3);

        shop1 = new Shop();
        shop1.setGame(game1);
        shop1.setPrice(39.99);
        shop1.setPictureShop("https://example.com/pictures/witcher3.jpg");
        shop1.setDescription("Epic RPG adventure");
        shop1 = shopRepository.save(shop1);

        shop2 = new Shop();
        shop2.setGame(game2);
        shop2.setPrice(59.99);
        shop2.setPictureShop("https://example.com/pictures/cyberpunk.jpg");
        shop2.setDescription("Futuristic open world");
        shop2 = shopRepository.save(shop2);
    }

    @Test
    void testExistsByGame_WithExistingGame_ShouldReturnTrue() {
        Boolean exists = shopRepository.existsByGame(game1);

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByGame_WithNonExistingGame_ShouldReturnFalse() {
        Boolean exists = shopRepository.existsByGame(game3);

        assertThat(exists).isFalse();
    }

    @Test
    void testGetAllFromShop_ShouldReturnAllShopEntries() {
        Optional<List<Shop>> result = shopRepository.getAllFromShop();

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(shop -> shop.getGame().getName())
            .containsExactlyInAnyOrder("The Witcher 3: Wild Hunt", "Cyberpunk 2077");
    }

    @Test
    void testGetAllFromShopByGameNameFilter_WithPartialName_ShouldFindShops() {
        Optional<List<Shop>> result = shopRepository.getAllFromShopByGameNameFilter("witcher");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getGame().getName()).contains("Witcher");
    }

    @Test
    void testGetAllFromShopByGameNameFilter_CaseInsensitive_ShouldFindShops() {
        Optional<List<Shop>> result = shopRepository.getAllFromShopByGameNameFilter("CYBERPUNK");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getGame().getName()).isEqualTo("Cyberpunk 2077");
    }

    @Test
    void testGetAllFromShopByGameNameFilter_WithEmptyString_ShouldReturnAll() {
        Optional<List<Shop>> result = shopRepository.getAllFromShopByGameNameFilter("");

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    void testGetAllFromShopByGameNameFilter_WithNonExistentGame_ShouldReturnEmpty() {
        Optional<List<Shop>> result = shopRepository.getAllFromShopByGameNameFilter("NonExistent");

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testGetShopByGameName_WithExistingGame_ShouldReturnShop() {
        Optional<Shop> result = shopRepository.getShopByGameName("The Witcher 3: Wild Hunt");

        assertThat(result).isPresent();
        assertThat(result.get().getGame().getName()).isEqualTo("The Witcher 3: Wild Hunt");
        assertThat(result.get().getPrice()).isEqualTo(39.99);
    }

    @Test
    void testGetShopByGameName_WithNonExistentGame_ShouldReturnEmpty() {
        Optional<Shop> result = shopRepository.getShopByGameName("Non Existent Game");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetGameShopPictureByGameName_WithExistingGame_ShouldReturnPicture() {
        String picture = shopRepository.getGameShopPictureByGameName("Cyberpunk 2077");

        assertThat(picture).isEqualTo("https://example.com/pictures/cyberpunk.jpg");
    }

    @Test
    void testGetGameShopPictureByGameName_WithNonExistentGame_ShouldReturnNull() {
        String picture = shopRepository.getGameShopPictureByGameName("Non Existent Game");

        assertThat(picture).isNull();
    }

    @Test
    void testSaveShop_ShouldPersistShop() {
        Shop newShop = new Shop();
        newShop.setGame(game3);
        newShop.setPrice(49.99);
        newShop.setPictureShop("https://example.com/pictures/eldenring.jpg");
        newShop.setDescription("Challenging action RPG");

        Shop saved = shopRepository.save(newShop);
        entityManager.flush();
        entityManager.clear();

        Optional<Shop> found = shopRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGame().getName()).isEqualTo("Elden Ring");
        assertThat(found.get().getPrice()).isEqualTo(49.99);
    }

    @Test
    void testDeleteShop_ShouldRemoveShop() {
        Long shopId = shop1.getId();

        shopRepository.delete(shop1);
        shopRepository.flush();
        entityManager.clear();

        Optional<Shop> found = shopRepository.findById(shopId);
        assertThat(found).isEmpty();
    }
}