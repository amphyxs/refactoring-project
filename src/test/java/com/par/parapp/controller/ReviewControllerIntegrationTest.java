package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.AbstractPostgresTestContainer;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userJwtToken;
    private String userLogin;
    private String devLogin;
    private String testGameName;

    @BeforeEach
    void setUp() throws Exception {
        String shortId = UUID.randomUUID().toString().substring(0, 6);
        devLogin = "d_" + shortId;
        userLogin = "u_" + shortId;
        testGameName = "g_" + shortId;

        // Регистрация разработчика
        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", devLogin + "@example.com", true);
        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devSignUp)));

        // Вход разработчика
        SignInRequest devSignIn = new SignInRequest();
        devSignIn.setLogin(devLogin);
        devSignIn.setPassword("devpass123");

        MvcResult devResult = mockMvc.perform(post("/auth/sign-in")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devSignIn)))
                .andReturn();

        String devJwtToken = objectMapper.readTree(devResult.getResponse().getContentAsString()).get("jwt").asText();

        // Загрузка игры
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName(testGameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://testgame_" + shortId + ".com");
        uploadRequest.setPrice(24.99);
        uploadRequest.setDescription("Test game for reviews");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Strategy")));

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)));

        // Регистрация пользователя
        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass123", userLogin + "@example.com", false);
        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignUp)));

        // Вход пользователя
        SignInRequest userSignIn = new SignInRequest();
        userSignIn.setLogin(userLogin);
        userSignIn.setPassword("userpass123");

        MvcResult userResult = mockMvc.perform(post("/auth/sign-in")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSignIn)))
                .andReturn();

        userJwtToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("jwt").asText();

        // Пополнение баланса
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setBalance(100.0);
        mockMvc.perform(post("/user/balance-add")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)));

        // Покупка игры
        GameNameRequest gameRequest = new GameNameRequest(testGameName, false);
        mockMvc.perform(post("/game")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameRequest)));
    }

    @Test
    void testAddReview() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setGameName(testGameName);
        reviewRequest.setReviewText("This is a great game! Highly recommended.");

        mockMvc.perform(post("/review")
                        .header("Origin", "http://localhost:3000")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetReviewsByGame() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setGameName(testGameName);
        reviewRequest.setReviewText("Amazing gameplay!");

        mockMvc.perform(post("/review")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewRequest)));

        mockMvc.perform(get("/review")
                        .header("Origin", "http://localhost:3000")
                        .param("selectedGame", testGameName)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAddReviewWithoutAuthentication() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setGameName(testGameName);
        reviewRequest.setReviewText("Anonymous review");

        mockMvc.perform(post("/review")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testAddReviewWithInvalidData() throws Exception {
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setGameName("");
        reviewRequest.setReviewText("");

        mockMvc.perform(post("/review")
                        .header("Origin", "http://localhost:3000")
                        .header("Authorization", "Bearer " + userJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }
}