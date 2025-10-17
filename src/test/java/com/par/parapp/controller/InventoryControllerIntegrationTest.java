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
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String devJwtToken;
    private String userJwtToken;
    private final String devLogin = "invdev";
    private final String userLogin = "invuser";
    private final String gameName = "InventoryTestGame";

    @BeforeAll
    void setUp() throws Exception {
        SignUpRequest devSignUp = new SignUpRequest(devLogin, "devpass123", "invdev@example.com", true);
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devSignUp)));

        SignInRequest devSignIn = new SignInRequest();
        devSignIn.setLogin(devLogin);
        devSignIn.setPassword("devpass123");
        
        MvcResult devResult = mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(devSignIn)))
                .andReturn();
        
        devJwtToken = objectMapper.readTree(devResult.getResponse().getContentAsString()).get("jwt").asText();

        UploadGameRequest uploadRequest = new UploadGameRequest();
        uploadRequest.setName(gameName);
        uploadRequest.setDevLogin(devLogin);
        uploadRequest.setGameUrl("https://inventorygame.com");
        uploadRequest.setPrice(39.99);
        uploadRequest.setDescription("Game with inventory");
        uploadRequest.setPictureCover("https://example.com/cover.jpg");
        uploadRequest.setPictureShop("https://example.com/shop.jpg");
        uploadRequest.setPictureGameplay1("https://example.com/gameplay1.jpg");
        uploadRequest.setPictureGameplay2("https://example.com/gameplay2.jpg");
        uploadRequest.setPictureGameplay3("https://example.com/gameplay3.jpg");
        uploadRequest.setGenres(new HashSet<>(Arrays.asList("RPG")));
        uploadRequest.setCommonItemName("Health Potion");
        uploadRequest.setCommonItemUrl("https://example.com/potion.jpg");
        uploadRequest.setRareItemName("Magic Scroll");
        uploadRequest.setRareItemUrl("https://example.com/scroll.jpg");
        uploadRequest.setLegendaryItemName("Legendary Amulet");
        uploadRequest.setLegendaryItemUrl("https://example.com/amulet.jpg");

        mockMvc.perform(post("/dev")
                .header("Authorization", "Bearer " + devJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uploadRequest)));

        // Register user
        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass123", "invuser@example.com", false);
        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignUp)));

        SignInRequest userSignIn = new SignInRequest();
        userSignIn.setLogin(userLogin);
        userSignIn.setPassword("userpass123");
        
        MvcResult userResult = mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userSignIn)))
                .andReturn();
        
        userJwtToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("jwt").asText();

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setBalance(100.0);
        
        mockMvc.perform(post("/user/balance-add")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)));

        GameNameRequest buyRequest = new GameNameRequest(gameName, false);
        mockMvc.perform(post("/game")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyRequest)));

        GameNameRequest enterRequest = new GameNameRequest(gameName, false);
        mockMvc.perform(post("/library/enter")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enterRequest)));
    }

    @Test
    void testGetAllItemsWithDefaultPagination() throws Exception {
        mockMvc.perform(get("/inventory")
                .header("Authorization", "Bearer " + userJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetAllItemsWithCustomPagination() throws Exception {
        mockMvc.perform(get("/inventory")
                .header("Authorization", "Bearer " + userJwtToken)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetAllItemsPage1() throws Exception {
        mockMvc.perform(get("/inventory")
                .header("Authorization", "Bearer " + userJwtToken)
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetInventoryWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/inventory"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testGetInventoryWithLargePage() throws Exception {
        mockMvc.perform(get("/inventory")
                .header("Authorization", "Bearer " + userJwtToken)
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk());
    }
}
