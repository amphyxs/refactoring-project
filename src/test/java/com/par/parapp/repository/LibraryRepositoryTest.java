package com.par.parapp.repository;

import com.par.parapp.AbstractPostgresTestContainer;
import com.par.parapp.model.Game;
import com.par.parapp.model.Library;
import com.par.parapp.model.User;
import com.par.parapp.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LibraryRepositoryTest extends AbstractPostgresTestContainer {

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Game game1;
    private Game game2;
    private Game game3;
    private Library library1;
    private Library library2;

    @BeforeEach
    void setUp() {
        Wallet wallet = new Wallet(1000.0);
        wallet = walletRepository.save(wallet);

        testUser = new User(
            "librarytest",
            "active",
            "password123",
            "librarytest@example.com",
            LocalDate.now()
        );
        testUser.setWallet(wallet);
        testUser = userRepository.save(testUser);

        game1 = new Game();
        game1.setName("The Witcher 3: Wild Hunt");
        game1.setGameUrl("https://example.com/witcher3");
        game1 = gameRepository.save(game1);

        game2 = new Game();
        game2.setName("Cyberpunk 2077");
        game2.setGameUrl("https://example.com/cyberpunk");
        game2 = gameRepository.save(game2);

        game3 = new Game();
        game3.setName("Minecraft");
        game3.setGameUrl("https://example.com/minecraft");
        game3 = gameRepository.save(game3);

        library1 = new Library();
        library1.setUser(testUser);
        library1.setGame(game1);
        library1.setLastRunDate(Timestamp.valueOf(LocalDate.now().minusDays(5)
                .atTime(14, 30, 0)));
        library1 = libraryRepository.save(library1);

        library2 = new Library();
        library2.setUser(testUser);
        library2.setGame(game2);
        library2.setLastRunDate(Timestamp.valueOf(LocalDate.now().minusDays(2)
                .atTime(14, 30, 0)));
        library2 = libraryRepository.save(library2);
    }

    @Test
    void testGetAllFromLibraryByUserLogin_ShouldReturnAllGames() {
        Optional<List<Library>> result = libraryRepository.getAllFromLibraryByUserLogin(testUser.getLogin());

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).extracting(lib -> lib.getGame().getName())
            .containsExactlyInAnyOrder("The Witcher 3: Wild Hunt", "Cyberpunk 2077");
    }

    @Test
    void testGetAllFromLibraryByUserLogin_WithNonExistentUser_ShouldReturnEmpty() {
        Optional<List<Library>> result = libraryRepository.getAllFromLibraryByUserLogin("nonexistent");

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testFindByUser_WithPagination_ShouldReturnPage() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Optional<Page<Library>> result = libraryRepository.findByUser(testUser.getLogin(), pageRequest);

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).hasSize(2);
        assertThat(result.get().getTotalElements()).isEqualTo(2);
    }

    @Test
    void testGetAllFromLibraryByGameNameFilter_WithPartialName_ShouldFindGames() {
        Optional<List<Library>> result = libraryRepository.getAllFromLibraryByGameNameFilter(
            "witcher",
            testUser.getLogin()
        );
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getGame().getName()).contains("Witcher");
    }

    @Test
    void testGetAllFromLibraryByGameNameFilter_CaseInsensitive_ShouldFindGames() {
        Optional<List<Library>> result = libraryRepository.getAllFromLibraryByGameNameFilter(
            "CYBERPUNK",
            testUser.getLogin()
        );
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getGame().getName()).isEqualTo("Cyberpunk 2077");
    }

    @Test
    void testGetAllFromLibraryByGameNameFilter_WithNonExistentGame_ShouldReturnEmpty() {
        Optional<List<Library>> result = libraryRepository.getAllFromLibraryByGameNameFilter(
            "NonExistent",
            testUser.getLogin()
        );
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testEnterInGame_ShouldUpdateLastRunDate() {
        Timestamp oldDate = library1.getLastRunDate();

        libraryRepository.enterInGame(testUser.getLogin(), game1.getName());
        libraryRepository.flush();
        entityManager.clear();

        Library updated = libraryRepository.findById(library1.getId()).orElseThrow();
        assertThat(updated.getLastRunDate()).isNotEqualTo(oldDate);
    }

    @Test
    void testGetCountOfGames_ShouldReturnCorrectCount() {
        Integer count = libraryRepository.getCountOfGames(testUser.getLogin());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testGetCountOfGames_AfterAddingGame_ShouldIncrement() {
        Library library3 = new Library();
        library3.setUser(testUser);
        library3.setGame(game3);
        libraryRepository.save(library3);
        libraryRepository.flush();

        Integer count = libraryRepository.getCountOfGames(testUser.getLogin());

        assertThat(count).isEqualTo(3);
    }

    @Test
    void testGetCountOfGames_WithNonExistentUser_ShouldReturnZero() {
        Integer count = libraryRepository.getCountOfGames("nonexistent");

        assertThat(count).isEqualTo(0);
    }
}