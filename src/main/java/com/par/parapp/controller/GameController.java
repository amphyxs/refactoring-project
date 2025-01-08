package com.par.parapp.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.par.parapp.dto.GameNameRequest;
import com.par.parapp.model.Game;
import com.par.parapp.model.User;
import com.par.parapp.service.AuthService;
import com.par.parapp.service.GameService;
import com.par.parapp.service.InventoryService;
import com.par.parapp.service.LibraryService;
import com.par.parapp.service.ShopService;
import com.par.parapp.service.UserService;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameController {

    private final GameService gameService;

    private final UserService userService;

    private final LibraryService libraryService;

    private final InventoryService inventoryService;

    private final ShopService shopService;

    private final AuthService authService;

    public GameController(GameService gameService,
            UserService userService, LibraryService libraryService,
            InventoryService inventoryService, ShopService shopService,
            AuthService authService) {
        this.gameService = gameService;
        this.userService = userService;
        this.libraryService = libraryService;
        this.inventoryService = inventoryService;
        this.shopService = shopService;
        this.authService = authService;
    }

    @GetMapping("{gameName}")
    public ResponseEntity<Object> getGameInfo(@PathVariable String gameName) {
        return ResponseEntity.ok(gameService.getGameInfo(gameName));
    }

    @GetMapping("check/{gameName}")
    public ResponseEntity<Object> checkGameName(@PathVariable String gameName) {
        if (gameService.checkGameOnExist(gameName)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public ResponseEntity<Object> buyGame(@RequestBody GameNameRequest gameNameRequest,
            HttpServletRequest httpServletRequest) {
        String login = authService.getLoginFromToken(httpServletRequest);
        Game game = gameService.getGameByName(gameNameRequest.getGameName());
        User user = userService.getUserByLogin(login);
        libraryService.saveGameInUserLibrary(user, game);

        int status = inventoryService.saveInventory(user, game);

        libraryService.transferMoneyAndBonuses(user, game, gameNameRequest.getUseBonuses());

        return new ResponseEntity<>(status, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllGames(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(gameService.getAllGames(page, size));
    }

    /*
     * @PreAuthorize("hasAnyRole('USER','DEV')")
     * 
     * @GetMapping()
     * public ResponseEntity<Object> getAllGamesByLogin(@RequestParam(value =
     * "page",
     * defaultValue = "0") int page,
     * 
     * @RequestParam(value = "size", defaultValue = "10") int size,
     * HttpServletRequest httpServletRequest) {
     * String login = authService.getLoginFromToken(httpServletRequest);
     * return ResponseEntity.ok(libraryService.getUserGames(login, page, size));
     * }
     */
}
