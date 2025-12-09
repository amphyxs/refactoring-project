package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String devJwtToken;
    private String userJwtToken;
    private final String devLogin = "libdev1";
    private final String userLogin = "libuser1";
    private final String testGameName = "LibTestGame";

    @BeforeAll
    void setUp() throws Exception {
        // Register developer and upload game
        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", "libdev@example.com", true);
        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devSignUp)));

        SignInRequest devSignIn = new SignInRequest();
        devSignIn.setLogin(devLogin);
        devSignIn.setPassword("devpass123");
        
        MvcResult devResult = mockMvc.perform(post("/auth/sign-in")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devSignIn)))
                .andReturn();
        
        devJwtToken = objectMapper.readTree(devResult.getResponse().getContentAsString()).get("jwt").asText();

        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName(testGameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://testgame.com");
        uploadRequest.setPrice(19.99);
        uploadRequest.setDescription("Test game for library");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("RPG")));

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)));

        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass123", "libuser@example.com", false);
        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignUp)));

        SignInRequest userSignIn = new SignInRequest();
        userSignIn.setLogin(userLogin);
        userSignIn.setPassword("userpass123");
        
        MvcResult userResult = mockMvc.perform(post("/auth/sign-in")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignIn)))
                .andReturn();
        
        userJwtToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("jwt").asText();

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
                .content(objectMapper.writeValueAsString(gameRequest)));
    }

    @Test
    void testGetGamesByName() throws Exception {
        mockMvc.perform(get("/library")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .param("gameName", testGameName))
                .andExpect(status().isOk());
    }

    @Test
    void testEnterGame() throws Exception {
        mockMvc.perform(patch("/library/" + testGameName)
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetGamesCount() throws Exception {
        mockMvc.perform(get("/library/count/" + userLogin)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void testGetLastGames() throws Exception {
        mockMvc.perform(get("/library/last-games/" + userLogin)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testRefundGame() throws Exception {
        // Purchase a fresh game for refund test
        String refundGameName = "RefundTestGame";
        
        // Upload a new game
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName(refundGameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://refundtest.com");
        uploadRequest.setPrice(15.99);
        uploadRequest.setDescription("Game for refund test");
        uploadRequest.setPictureCover("https://example.com/cover2.jpg");
        uploadRequest.setPictureShop("https://example.com/shop2.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Action")));

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)));

        // Purchase the game
        GameNameRequest gameRequest = new GameNameRequest(refundGameName, false);
        mockMvc.perform(post("/game")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameRequest)));

        // Now refund it
        mockMvc.perform(post("/library/refund")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .param("gameName", refundGameName))
                .andExpect(status().isOk());
    }

    @Test
    void testEnterGameWithoutAuthentication() throws Exception {
        mockMvc.perform(patch("/library/" + testGameName)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().is5xxServerError()); 
    }
}
