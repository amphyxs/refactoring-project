package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EntityManager entityManager;

    private Game testGame1;
    private Game testGame2;

    @BeforeEach
    void setUp() {
        testGame1 = new Game();
        testGame1.setName("The Witcher 3");
        testGame1.setGameUrl("https://example.com/witcher3");
        testGame1 = gameRepository.save(testGame1);

        testGame2 = new Game();
        testGame2.setName("Cyberpunk 2077");
        testGame2.setGameUrl("https://example.com/cyberpunk");
        testGame2 = gameRepository.save(testGame2);
    }

    @Test
    void testGetByNameInAnyRegister_WithExactCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByNameInAnyRegister("The Witcher 3");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("The Witcher 3");
    }

    @Test
    void testGetByNameInAnyRegister_WithUpperCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByNameInAnyRegister("THE WITCHER 3");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("The Witcher 3");
    }

    @Test
    void testGetByNameInAnyRegister_WithLowerCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByNameInAnyRegister("the witcher 3");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("The Witcher 3");
    }

    @Test
    void testGetByNameInAnyRegister_WithMixedCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByNameInAnyRegister("tHe WiTcHeR 3");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("The Witcher 3");
    }

    @Test
    void testGetByNameInAnyRegister_WithNonExistentGame_ShouldReturnEmpty() {
        Optional<Game> result = gameRepository.getByNameInAnyRegister("Non Existent Game");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetByGameUrlInAnyRegister_WithExactCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByGameUrlInAnyRegister("https://example.com/witcher3");

        assertThat(result).isPresent();
        assertThat(result.get().getGameUrl()).isEqualTo("https://example.com/witcher3");
    }

    @Test
    void testGetByGameUrlInAnyRegister_WithUpperCase_ShouldFindGame() {
        Optional<Game> result = gameRepository.getByGameUrlInAnyRegister("HTTPS://EXAMPLE.COM/WITCHER3");

        assertThat(result).isPresent();
        assertThat(result.get().getGameUrl()).isEqualTo("https://example.com/witcher3");
    }

    @Test
    void testGetByGameUrlInAnyRegister_WithNonExistentUrl_ShouldReturnEmpty() {
        Optional<Game> result = gameRepository.getByGameUrlInAnyRegister("https://example.com/nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void testExistsByName_WithExistingGame_ShouldReturnTrue() {
        boolean exists = gameRepository.existsByName("The Witcher 3");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByName_WithNonExistingGame_ShouldReturnFalse() {
        boolean exists = gameRepository.existsByName("Non Existent Game");

        assertThat(exists).isFalse();
    }

    @Test
    void testGetGameByName_WithExistingGame_ShouldReturnGame() {
        Optional<Game> result = gameRepository.getGameByName("Cyberpunk 2077");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Cyberpunk 2077");
    }

    @Test
    void testGetGameByName_WithNonExistingGame_ShouldReturnEmpty() {
        Optional<Game> result = gameRepository.getGameByName("Non Existent Game");

        assertThat(result).isEmpty();
    }

    @Test
    void testSaveGame_ShouldPersistGame() {
        Game newGame = new Game();
        newGame.setName("Elden Ring");
        newGame.setGameUrl("https://example.com/eldenring");

        Game saved = gameRepository.save(newGame);
        entityManager.flush();
        entityManager.clear();

        Optional<Game> found = gameRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Elden Ring");
    }
}