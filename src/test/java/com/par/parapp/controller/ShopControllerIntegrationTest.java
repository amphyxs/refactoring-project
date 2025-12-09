package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class ShopControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String devLogin = "shopdev1";
    private final String testGameName = "ShopGame";

    @BeforeEach
    void setUp() throws Exception {
        // Register developer
        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", "shopdev@example.com", true);
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
        
        String devJwtToken = objectMapper.readTree(devResult.getResponse().getContentAsString()).get("jwt").asText();

        // Upload game to shop
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName(testGameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://shopgame.com");
        uploadRequest.setPrice(39.99);
        uploadRequest.setDescription("Test game for shop");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Adventure", "Puzzle")));

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)));
    }

    @Test
    void testGetGamesByNameAndGenres() throws Exception {
        mockMvc.perform(get("/shop")
                .header("Origin", "http://localhost:3000")
                .param("gameName", testGameName)
                .param("genres", "Adventure", "Puzzle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetGamesByNameAndSingleGenre() throws Exception {
        mockMvc.perform(get("/shop")
                .header("Origin", "http://localhost:3000")
                .param("gameName", testGameName)
                .param("genres", "Adventure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetGamesWithNonExistentGenre() throws Exception {
        mockMvc.perform(get("/shop")
                .header("Origin", "http://localhost:3000")
                .param("gameName", testGameName)
                .param("genres", "Horror"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetGamesWithEmptyName() throws Exception {
        mockMvc.perform(get("/shop")
                .param("gameName", "")
                .param("genres", "Adventure"))
                .andExpect(status().isOk());
    }
}
