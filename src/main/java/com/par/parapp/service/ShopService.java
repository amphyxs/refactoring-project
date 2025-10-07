package com.par.parapp.service;

import com.par.parapp.dto.ShopDataResponse;
import com.par.parapp.exception.ResourceAlreadyExist;
import com.par.parapp.exception.ResourceNotFoundException;
import com.par.parapp.model.Game;
import com.par.parapp.model.Genre;
import com.par.parapp.model.Shop;
import com.par.parapp.repository.ShopRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public void saveShop(Game game, Double price, String description,
            String pictureCover, String pictureShop, String pictureGameplay1,
            String pictureGameplay2, String pictureGameplay3) {

        if (shopRepository.existsByGame(game))
            throw new ResourceAlreadyExist("Данная игра уже опубликована!");
        shopRepository.save(new Shop(game, price, description, pictureCover, pictureShop,
                pictureGameplay1, pictureGameplay2, pictureGameplay3));

    }

    public List<ShopDataResponse> getEntriesByGameNameAndGenres(String gameName, List<String> genresReq) {
        List<Shop> allShopEntries;
        if (gameName.isEmpty())
            allShopEntries = shopRepository.getAllFromShop().orElseThrow(ResourceNotFoundException::new);
        else
            allShopEntries = shopRepository.getAllFromShopByGameNameFilter(gameName)
                    .orElseThrow(ResourceNotFoundException::new);

        List<ShopDataResponse> shopDataResponses = new ArrayList<>();

        allShopEntries.forEach(shop -> {

            List<String> genresResponse = shop.getGame().getGenres().stream()
                    .map(Genre::getName).toList();

            if (genresReq.isEmpty()) {
                shopDataResponses.add(new ShopDataResponse(shop.getGame().getName(), shop.getPrice(),
                        shop.getPicture_shop(), genresResponse.stream().toList()));
            } else if (new ArrayList<>(genresResponse).containsAll(genresReq)) {
                shopDataResponses.add(new ShopDataResponse(shop.getGame().getName(), shop.getPrice(),
                        shop.getPicture_shop(), genresResponse.stream().toList()));
            }

        });

        return shopDataResponses;
    }

    public String getGamePicture(String gameName) {
        return shopRepository.getGameShopPictureByGameName(gameName);
    }

}
