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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userJwtToken;
    private final String userLogin = "actuser";

    @BeforeEach
    void setUp() throws Exception {
        SignUpRequest userSignUp = new SignUpRequest(userLogin, "userpass123", "actuser@example.com", false);
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
    }

    @Test
    void testSubmitActivitySuccessfully() throws Exception {
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setText("Just finished playing an amazing game!");

        mockMvc.perform(post("/activity")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Запись успешно опубликована!"));
    }

    @Test
    void testGetAllActivitiesForUser() throws Exception {
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setText("New activity post");

        mockMvc.perform(post("/activity")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequest)));

        mockMvc.perform(get("/activity")
                .param("login", userLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetAllActivitiesWithPagination() throws Exception {
        mockMvc.perform(get("/activity")
                .param("login", userLogin)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitActivityWithoutAuthentication() throws Exception {
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setText("Unauthorized activity");

        mockMvc.perform(post("/activity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSubmitMultipleActivities() throws Exception {
        ActivityRequest activity1 = new ActivityRequest();
        activity1.setText("Started playing a new RPG");

        mockMvc.perform(post("/activity")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity1)))
                .andExpect(status().isCreated());

        ActivityRequest activity2 = new ActivityRequest();
        activity2.setText("Completed first quest");

        mockMvc.perform(post("/activity")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activity2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/activity")
                .param("login", userLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testGetActivitiesForNonExistentUser() throws Exception {
        mockMvc.perform(get("/activity")
                .param("login", "nonexistentuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testSubmitActivityWithLongText() throws Exception {
        ActivityRequest activityRequest = new ActivityRequest();
        activityRequest.setText("This is a very long activity post. ".repeat(50));

        mockMvc.perform(post("/activity")
                .header("Authorization", "Bearer " + userJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetActivitiesWithLargePage() throws Exception {
        mockMvc.perform(get("/activity")
                .param("login", userLogin)
                .param("page", "10")
                .param("size", "100"))
                .andExpect(status().isOk());
    }
}
