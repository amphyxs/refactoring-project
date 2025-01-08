package com.par.parapp.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.par.parapp.dto.ReviewRequest;
import com.par.parapp.model.Game;
import com.par.parapp.model.User;
import com.par.parapp.service.AuthService;
import com.par.parapp.service.GameService;
import com.par.parapp.service.ReviewService;
import com.par.parapp.service.UserService;

@RestController
@RequestMapping("/review")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {
    private final GameService gameService;

    private final UserService userService;

    private final ReviewService reviewService;
    private final AuthService authService;

    public ReviewController(GameService gameService, UserService userService, ReviewService reviewService,
            AuthService authService) {
        this.gameService = gameService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.authService = authService;
    }

    @GetMapping()
    public ResponseEntity<Object> getGamesBySelectedGame(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String selectedGame) {

        return ResponseEntity.ok(reviewService.getReviewsByCondition(selectedGame, page, size));
    }

    @PostMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> addReview(@Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest httpServletRequest) {
        String login = authService.getLoginFromToken(httpServletRequest);
        User user = userService.getUserByLogin(login);
        Game game = gameService.getGameByName(reviewRequest.getGameName());
        reviewService.saveReview(user, game, reviewRequest.getReviewText());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
