package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.*;
import com.par.parapp.model.*;
import com.par.parapp.repository.GameRepository;
import com.par.parapp.repository.GenreRepository;
import com.par.parapp.repository.RoleRepository;
import com.par.parapp.repository.ShopRepository;
import com.par.parapp.repository.UserRepository;
import com.par.parapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testGameName;
    private String devLogin;
    private String regularUserLogin;
    private String userJwtToken;
    private Double testGamePrice = 29.99;

    @BeforeAll
    void setUp() throws Exception {
        long timestamp = System.currentTimeMillis();
        devLogin = "dv" + (timestamp % 10000);
        regularUserLogin = "us" + (timestamp % 10000);
        testGameName = "TestGame" + (timestamp % 100000);
        
        Role devRole = roleRepository.findByName(ERole.ROLE_DEV)
                .orElseThrow(() -> new RuntimeException("DEV role not found"));
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        
        Wallet devWallet = new Wallet();
        devWallet.setBalance(0.0);
        devWallet.setBonuses(0.0);
        devWallet = walletRepository.save(devWallet);
        
        Wallet userWallet = new Wallet();
        userWallet.setBalance(0.0);
        userWallet.setBonuses(0.0);
        userWallet = walletRepository.save(userWallet);
        
        User devUser = new User();
        devUser.setLogin(devLogin);
        devUser.setPassword(passwordEncoder.encode("devpass123"));
        devUser.setEmail(devLogin + "@test.com");
        devUser.setRegistrationDate(LocalDate.now());
        devUser.setStatus("active");
        devUser.setWallet(devWallet);
        Set<Role> devRoles = new HashSet<>();
        devRoles.add(devRole);
        devUser.setRoles(devRoles);
        devUser = userRepository.save(devUser);
        
        User regularUser = new User();
        regularUser.setLogin(regularUserLogin);
        regularUser.setPassword(passwordEncoder.encode("userpass123"));
        regularUser.setEmail(regularUserLogin + "@test.com");
        regularUser.setRegistrationDate(LocalDate.now());
        regularUser.setStatus("active");
        regularUser.setWallet(userWallet);
        Set<Role> regularRoles = new HashSet<>();
        regularRoles.add(userRole);
        regularUser.setRoles(regularRoles);
        userRepository.save(regularUser);
        
        SignInRequest userSignIn = new SignInRequest();
        userSignIn.setLogin(regularUserLogin);
        userSignIn.setPassword("userpass123");
        
        String response = mockMvc.perform(post("/auth/sign-in")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(userSignIn)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        userJwtToken = objectMapper.readTree(response).get("jwt").asText();
        
        Genre actionGenre = genreRepository.findByName("Action")
                .orElseThrow(() -> new RuntimeException("Action genre not found"));
        Genre adventureGenre = genreRepository.findByName("Adventure")
                .orElseThrow(() -> new RuntimeException("Adventure genre not found"));
        
        Set<Genre> genres = new HashSet<>();
        genres.add(actionGenre);
        genres.add(adventureGenre);
        
        Game game = new Game();
        game.setName(testGameName);
        game.setUser(devUser);
        game.setGameUrl("https://testgame.com");
        game.setDevelopmentDate(LocalDate.now());
        game.setGenres(genres);
        game = gameRepository.save(game);
        
        Shop shop = new Shop();
        shop.setGame(game);
        shop.setPrice(testGamePrice);
        shop.setDescription("Test game description");
        shop.setPictureCover("https://example.com/cover.jpg");
        shop.setPictureShop("https://example.com/shop.jpg");
        shop.setPictureGamePlay1("https://example.com/gameplay1.jpg");
        shop.setPictureGamePlay2("https://example.com/gameplay2.jpg");
        shop.setPictureGamePlay3("https://example.com/gameplay3.jpg");
        shopRepository.save(shop);
    }

    @Test
    void testGetGameInfo() throws Exception {
        mockMvc.perform(get("/game/" + testGameName)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName").value(testGameName))
                .andExpect(jsonPath("$.devLogin").value(devLogin));
    }

    @Test
    void testCheckGameExists() throws Exception {
        mockMvc.perform(get("/game/check/" + testGameName)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCheckGameNotExists() throws Exception {
        mockMvc.perform(get("/game/check/NonExistentGame")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllGames() throws Exception {
        mockMvc.perform(get("/game")
                .header("Origin", "http://localhost:3000")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testBuyGame() throws Exception {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setBalance(100.0);
        
        mockMvc.perform(post("/user/balance-add")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)));

        GameNameRequest gameRequest = new GameNameRequest(testGameName, false);

        mockMvc.perform(post("/game")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testBuyGameWithoutAuthentication() throws Exception {
        GameNameRequest gameRequest = new GameNameRequest(testGameName, false);

        mockMvc.perform(post("/game")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testGetGameInfoNonExistent() throws Exception {
        mockMvc.perform(get("/game/NonExistentGame")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().is5xxServerError());
    }
}
