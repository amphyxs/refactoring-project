package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.BalanceRequest;
import com.par.parapp.dto.SignInRequest;
import com.par.parapp.dto.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private final String testUserLogin = "usertest1";
    private final String testUserPassword = "password123";

    @BeforeEach
    void setUp() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest(
            testUserLogin,
            testUserPassword,
            "usertest1@example.com",
            false
        );

        mockMvc.perform(post("/auth/sign-up")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin(testUserLogin);
        signInRequest.setPassword(testUserPassword);

        MvcResult result = mockMvc.perform(post("/auth/sign-in")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("jwt").asText();
    }

    @Test
    void testGetUserLogin() throws Exception {
        mockMvc.perform(get("/user")
                .header("Origin", "http://localhost:3000")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(testUserLogin));
    }

    @Test
    void testCheckUserExists() throws Exception {
        mockMvc.perform(get("/user/exist/" + testUserLogin)
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCheckUserNotExists() throws Exception {
        mockMvc.perform(get("/user/exist/nonexistentuser12345")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStatusAndDates() throws Exception {
        mockMvc.perform(get("/user/status-dates/" + testUserLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.registrationDate").exists());
    }

    @Test
    void testGetUserBalance() throws Exception {
        mockMvc.perform(get("/user/balance")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.bonuses").exists());
    }

    @Test
    void testAddBalance() throws Exception {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setBalance(100.0);

        mockMvc.perform(post("/user/balance-add")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/user/balance")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(patch("/user/logout/" + testUserLogin)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateIsTutorialCompleted() throws Exception {
        mockMvc.perform(patch("/user/is-tutorial-completed")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("true"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testAddBalanceWithoutAuthentication() throws Exception {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setBalance(100.0);

        mockMvc.perform(post("/user/balance-add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(balanceRequest)))
                .andExpect(status().is5xxServerError());
    }
}
