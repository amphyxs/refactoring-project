package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.SignInRequest;
import com.par.parapp.dto.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignUpAndSignIn() throws Exception {
        // Register a new user
        SignUpRequest signUpRequest = new SignUpRequest(
            "testuser", // 5-10 characters
            "testpass123", // 8-255 characters
            "testuser@example.com",
            false
        );

        mockMvc.perform(post("/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());

        // Sign in with the registered user
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setLogin("testuser"); // Must be 5-10 characters
        signInRequest.setPassword("testpass123"); // Must be at least 8 characters

        mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.login").value("testuser"));
    }
}
