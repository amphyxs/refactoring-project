package com.par.parapp.service;

import com.par.parapp.dto.GamePictures;
import com.par.parapp.dto.UploadGameRequest;
import com.par.parapp.model.Game;
import org.springframework.stereotype.Service;

@Service
public class DevService {

    private final GameService gameService;

    private final ShopService shopService;

    private final ItemService itemService;

    public DevService(GameService gameService, ShopService shopService, ItemService itemService) {
        this.gameService = gameService;
        this.shopService = shopService;
        this.itemService = itemService;
    }

    public void uploadGame(UploadGameRequest uploadGameRequest) {
        Game game = gameService.saveGame(uploadGameRequest.getName(), uploadGameRequest.getGenres(),
                uploadGameRequest.getDevLogin(), uploadGameRequest.getGameUrl());

        var pictures = new GamePictures();
        pictures.pictureCover = uploadGameRequest.getPictureCover();
        pictures.pictureShop = uploadGameRequest.getPictureShop();
        pictures.pictureGameplay1 = uploadGameRequest.getPictureGameplay1();
        pictures.pictureGameplay2 = uploadGameRequest.getPictureGameplay2();
        pictures.pictureGameplay3 = uploadGameRequest.getPictureGameplay3();

        shopService.saveShop(game, uploadGameRequest.getPrice(), uploadGameRequest.getDescription(),
                pictures);

        if (!uploadGameRequest.getCommonItemName().isEmpty() && !uploadGameRequest.getCommonItemUrl().isEmpty())
            itemService.saveItem(game, uploadGameRequest.getCommonItemName(),
                    "Обычная", uploadGameRequest.getCommonItemUrl());

        if (!uploadGameRequest.getRareItemName().isEmpty() && !uploadGameRequest.getRareItemUrl().isEmpty())
            itemService.saveItem(game, uploadGameRequest.getRareItemName(),
                    "Редкая", uploadGameRequest.getRareItemUrl());

        if (!uploadGameRequest.getLegendaryItemName().isEmpty() && !uploadGameRequest.getLegendaryItemUrl().isEmpty())
            itemService.saveItem(game, uploadGameRequest.getLegendaryItemName(),
                    "Легендарная", uploadGameRequest.getLegendaryItemUrl());
    }
}
