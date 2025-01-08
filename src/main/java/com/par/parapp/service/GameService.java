package com.par.parapp.service;

import com.par.parapp.dto.GameInfoResponse;
import com.par.parapp.dto.GamePictures;
import com.par.parapp.exception.*;
import com.par.parapp.model.*;
import com.par.parapp.repository.GameRepository;
import com.par.parapp.repository.GenreRepository;
import com.par.parapp.repository.ShopRepository;
import com.par.parapp.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GameService {

    private final GameRepository gameRepository;

    private final GenreRepository genreRepository;

    private final UserRepository userRepository;

    private final ShopRepository shopRepository;

    public GameService(GameRepository gameRepository, GenreRepository genreRepository, UserRepository userRepository,
            ShopRepository shopRepository) {
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
    }

    public Game saveGame(String name, Set<String> reqGenres, String devLogin, String gameUrl) {

        if (gameRepository.getByNameInAnyRegister(name).isPresent()
                || gameRepository.getByGameUrlInAnyRegister(gameUrl).isPresent())
            throw new ResourceAlreadyExist("Данная игра уже опубликована!");

        Set<Genre> genres = new HashSet<>();

        reqGenres.forEach(r -> {
            Genre genre = genreRepository
                    .findByName(r)
                    .orElseThrow(ResourceNotFoundException::new);
            genres.add(genre);
        });

        Game game = new Game(name, LocalDate.now());
        game.setGenres(genres);

        User dev = userRepository.findByLogin(devLogin).orElseThrow(ResourceNotFoundException::new);
        game.setUser(dev);
        game.setGameUrl(gameUrl);
        gameRepository.save(game);

        return game;
    }

    public GameInfoResponse getGameInfo(String gameName) {
        Shop shop = shopRepository.getShopByGameName(gameName).orElseThrow(ResourceNotFoundException::new);
        String gameNameResp = shop.getGame().getName();
        Double priceResp = shop.getPrice();
        List<String> genresResp = shop.getGame().getGenres().stream()
                .map(Genre::getName).toList();
        String developmentDateResp = shop.getGame().getDevelopmentDate().toString();
        String devLoginResp = shop.getGame().getUser().getLogin();
        String gameDescriptionResp = shop.getDescription();
        String pictureCoverResp = shop.getPictureCover();
        String pictureGameplay1Resp = shop.getPictureGamePlay1();
        String pictureGameplay2Resp = shop.getPictureGamePlay2();
        String pictureGameplay3Resp = shop.getPictureGamePlay3();

        var pictures = new GamePictures(
                null,
                pictureCoverResp,
                pictureGameplay1Resp,
                pictureGameplay2Resp,
                pictureGameplay3Resp);

        return new GameInfoResponse(gameNameResp, priceResp, genresResp,
                developmentDateResp, devLoginResp, gameDescriptionResp,
                pictures);

    }

    public Game getGameByName(String gameName) {
        return gameRepository.getGameByName(gameName).orElseThrow(ResourceNotFoundException::new);
    }

    public boolean checkGameOnExist(String gameName) {
        return gameRepository.existsByName(gameName);
    }

    public List<Game> getAllGames(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return gameRepository.findAll(pageable).getContent();
    }

}
