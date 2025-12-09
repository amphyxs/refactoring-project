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
class GuideControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String devJwtToken;
    private String userJwtToken;
    private final String devLogin = "guidedev";
    private final String userLogin = "guideuser";
    private final String gameName = "GuideTestGame";

    @BeforeAll
    void setUp() throws Exception {
        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", "guidedev@example.com", true);
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
        uploadRequest.setName(gameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://guidegame.com");
        uploadRequest.setPrice(29.99);
        uploadRequest.setDescription("Game with guides");
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

        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass123", "guideuser@example.com", false);
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
    }

    @Test
    void testAddGuideSuccessfully() throws Exception {
        GuideRequest guideRequest = new GuideRequest();
        guideRequest.setGameName(gameName);
        guideRequest.setGuideText("This is a comprehensive guide for the game. Follow these steps to win!");

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetGuidesByGame() throws Exception {
        GuideRequest guideRequest = new GuideRequest();
        guideRequest.setGameName(gameName);
        guideRequest.setGuideText("Complete beginner guide");

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideRequest)));

        mockMvc.perform(get("/guide")
                .header("Origin", "http://localhost:3000")
                .param("selectedGame", gameName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetGuidesWithPagination() throws Exception {
        mockMvc.perform(get("/guide")
                .header("Origin", "http://localhost:3000")
                .param("selectedGame", gameName)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddGuideWithoutAuthentication() throws Exception {
        GuideRequest guideRequest = new GuideRequest();
        guideRequest.setGameName(gameName);
        guideRequest.setGuideText("Unauthorized guide");

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideRequest)))
                .andExpect(status().is5xxServerError()); 
    }

    @Test
    void testAddMultipleGuides() throws Exception {
        GuideRequest guide1 = new GuideRequest();
        guide1.setGameName(gameName);
        guide1.setGuideText("Beginner's guide");

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guide1)))
                .andExpect(status().isCreated());

        GuideRequest guide2 = new GuideRequest();
        guide2.setGameName(gameName);
        guide2.setGuideText("Advanced strategies");

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guide2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/guide")
                .header("Origin", "http://localhost:3000")
                .param("selectedGame", gameName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetGuidesForNonExistentGame() throws Exception {
        mockMvc.perform(get("/guide")
                .header("Origin", "http://localhost:3000")
                .param("selectedGame", "NonExistentGame"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testAddGuideWithLongText() throws Exception {
        GuideRequest guideRequest = new GuideRequest();
        guideRequest.setGameName(gameName);
        guideRequest.setGuideText("This is a very long guide. ".repeat(35));

        mockMvc.perform(post("/guide")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideRequest)))
                .andExpect(status().isCreated());
    }
}
