package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiServiceImplTests {

    private final AiServiceImpl aiService = new AiServiceImpl();

    @Test
    void shouldGenerateDigitalProductDescription() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest();
        request.setName("iPad Air 5");
        request.setCategory("数码产品");
        request.setConditionLevel("九成新");
        request.setUsedTime("一年");
        request.setAccessories("充电器、保护壳");
        request.setPrice(new BigDecimal("2800"));
        request.setCampusTrade(true);

        GenerateDescriptionResponse response = aiService.generateDescription(request);

        assertEquals(
                "这是一件九成新的 iPad Air 5，已使用约一年，外观保存良好，功能正常。" +
                        "商品包含充电器、保护壳，适合学习、办公、网课和日常娱乐。" +
                        "目前售价为 2800 元，支持校内线下面交，有意者可以留言联系。",
                response.getDescription());
    }

    @Test
    void shouldOmitEmptyOptionalFields() {
        GenerateDescriptionRequest request = new GenerateDescriptionRequest();
        request.setName("高等数学教材");
        request.setCategory("教材书籍");
        request.setAccessories(" ");
        request.setPrice(BigDecimal.ZERO);
        request.setCampusTrade(false);

        String description = aiService.generateDescription(request).getDescription();

        assertTrue(description.contains("适合课程学习、复习备考和资料查阅"));
        assertFalse(description.contains("null"));
        assertFalse(description.contains("商品包含"));
        assertFalse(description.contains("已使用约"));
        assertFalse(description.contains("售价"));
        assertFalse(description.contains("支持校内线下面交"));
    }

    @Test
    void shouldHandleNullRequest() {
        String description = aiService.generateDescription(null).getDescription();

        assertTrue(description.contains("二手商品"));
        assertTrue(description.contains("适合校园二手交易使用"));
    }
}
