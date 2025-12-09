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
class DevControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String devJwtToken;
    private String userJwtToken;
    private String devLogin;
    private String userLogin;

    @BeforeEach
    void setUp() throws Exception {
        long timestamp = System.currentTimeMillis();
        devLogin = "dev" + (timestamp % 100000);
        userLogin = "usr" + (timestamp % 100000);

        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", devLogin + "@test.com", true);
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

        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass", userLogin + "@test.com", false);
        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignUp)));

        SignInRequest userSignIn = new SignInRequest();
        userSignIn.setLogin(userLogin);
        userSignIn.setPassword("userpass");

        MvcResult userResult = mockMvc.perform(post("/auth/sign-in")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignIn)))
                .andReturn();
        
        userJwtToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("jwt").asText();
    }

    @Test
    void testUploadGameWithoutAuthentication() throws Exception {
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName("UnauthorizedGame");
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://unauthorized.com");
        uploadRequest.setPrice(19.99);
        uploadRequest.setDescription("Should not be uploaded");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Strategy")));

        MvcResult result = mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
            status == 401 || status == 403 || status == 500,
            "Expected unauthorized/forbidden/error status, got: " + status
        );
    }

    @Test
    void testUploadGameAsRegularUser() throws Exception {
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName("UserGame");
        uploadRequest.setDevLogin(userLogin);
        uploadRequest.setGameUrl("https://usergame.com");
        uploadRequest.setPrice(19.99);
        uploadRequest.setDescription("User trying to upload");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Simulation")));

        MvcResult result = mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
            status == 403 || status == 500,
            "Expected forbidden/error status for USER role, got: " + status
        );
    }

    @Test
    void testUploadGameWithInvalidPrice() throws Exception {
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName("ExpensiveGame");
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://expensive.com");
        uploadRequest.setPrice(999.99); // Exceeds max price of 500
        uploadRequest.setDescription("Too expensive");
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
                .content(objectMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadGameWithMissingRequiredFields() throws Exception {
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName("IncompleteGame");

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadGameWithInvalidURL() throws Exception {
        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName("InvalidURLGame");
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("not-a-valid-url");
        uploadRequest.setPrice(29.99);
        uploadRequest.setDescription("Game with invalid URL");
        uploadRequest.setPictureCover("not-a-url");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("Action")));

        mockMvc.perform(post("/dev")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)))
                .andExpect(status().isBadRequest());
    }
}
