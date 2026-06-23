package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.service.impl.AiServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiControllerTests {

    @Test
    void shouldGenerateDescriptionThroughApi() throws Exception {
        AiController controller = new AiController();
        ReflectionTestUtils.setField(controller, "aiService", new AiServiceImpl());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/ai/generate-description")
                        .contentType("application/json")
                        .content("{"
                                + "\"name\":\"iPad Air 5\","
                                + "\"category\":\"数码产品\","
                                + "\"conditionLevel\":\"九成新\","
                                + "\"usedTime\":\"一年\","
                                + "\"accessories\":\"充电器、保护壳\","
                                + "\"price\":2800,"
                                + "\"campusTrade\":true"
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_code").value(1))
                .andExpect(jsonPath("$.data.description").value(
                        "这是一件九成新的 iPad Air 5，已使用约一年，外观保存良好，功能正常。"
                                + "商品包含充电器、保护壳，适合学习、办公、网课和日常娱乐。"
                                + "目前售价为 2800 元，支持校内线下面交，有意者可以留言联系。"));
    }

    @Test
    void shouldSuggestPriceThroughApi() throws Exception {
        AiController controller = new AiController();
        ReflectionTestUtils.setField(controller, "aiService", new AiServiceImpl());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/ai/suggest-price")
                        .contentType("application/json")
                        .content("{"
                                + "\"name\":\"iPad Air 5\","
                                + "\"category\":\"数码产品\","
                                + "\"originalPrice\":4500,"
                                + "\"conditionLevel\":\"九成新\","
                                + "\"usedTime\":\"一年\","
                                + "\"price\":2800"
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_code").value(1))
                .andExpect(jsonPath("$.data.minPrice").value(2700.00))
                .andExpect(jsonPath("$.data.maxPrice").value(3150.00))
                .andExpect(jsonPath("$.data.suggestedPrice").value(2925.00))
                .andExpect(jsonPath("$.data.priceStatus").value("NORMAL"));
    }
}
