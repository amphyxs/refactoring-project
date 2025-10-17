package com.par.parapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.par.parapp.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String gameName = "TestMarketGame";

    @Test
    void testSellItemWithoutAuthentication() throws Exception {
        ItemSellRequest sellRequest = new ItemSellRequest();
        sellRequest.setGameName(gameName);
        sellRequest.setItemName("Iron Sword");
        sellRequest.setRarity("Обычная");
        sellRequest.setPrice(15.0);

        mockMvc.perform(post("/market/sell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sellRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testBuyItemWithoutAuthentication() throws Exception {
        ItemBuyRequest buyRequest = new ItemBuyRequest();
        buyRequest.setGameName(gameName);
        buyRequest.setItemName("Iron Sword");
        buyRequest.setRarity("Обычная");
        buyRequest.setMarketId(1L);

        mockMvc.perform(post("/market/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyRequest)))
                .andExpect(status().is5xxServerError());
    }
}
