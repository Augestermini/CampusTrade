package com.second.hand.trading.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.client.DeepSeekClient;
import com.second.hand.trading.server.dto.GenerateDescriptionRequest;
import com.second.hand.trading.server.dto.GenerateDescriptionResponse;
import com.second.hand.trading.server.dto.SuggestPriceRequest;
import com.second.hand.trading.server.dto.SuggestPriceResponse;
import com.second.hand.trading.server.dto.TradeAdviceRequest;
import com.second.hand.trading.server.dto.TradeAdviceResponse;
import com.second.hand.trading.server.prompt.AiPromptBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldSuggestNormalDigitalProductPrice() {
        SuggestPriceRequest request = new SuggestPriceRequest();
        request.setName("iPad Air 5");
        request.setCategory("数码产品");
        request.setOriginalPrice(new BigDecimal("4500"));
        request.setConditionLevel("九成新");
        request.setUsedTime("一年");
        request.setPrice(new BigDecimal("2800"));

        SuggestPriceResponse response = aiService.suggestPrice(request);

        assertEquals(new BigDecimal("2700.00"), response.getMinPrice());
        assertEquals(new BigDecimal("3150.00"), response.getMaxPrice());
        assertEquals(new BigDecimal("2925.00"), response.getSuggestedPrice());
        assertEquals("NORMAL", response.getPriceStatus());
        assertTrue(response.getReason().contains("60% - 70%"));
        assertTrue(response.getReason().contains("电池、屏幕和功能情况"));
    }

    @Test
    void shouldApplyCategoryAndUsedTimeAdjustments() {
        SuggestPriceRequest request = new SuggestPriceRequest();
        request.setCategory("教材书籍");
        request.setOriginalPrice(new BigDecimal("100"));
        request.setConditionLevel("八成新");
        request.setUsedTime("两年");
        request.setPrice(new BigDecimal("50"));

        SuggestPriceResponse response = aiService.suggestPrice(request);

        assertEquals(new BigDecimal("40.00"), response.getMinPrice());
        assertEquals(new BigDecimal("45.00"), response.getMaxPrice());
        assertEquals(new BigDecimal("42.50"), response.getSuggestedPrice());
        assertEquals("TOO_HIGH", response.getPriceStatus());
    }

    @Test
    void shouldReturnUnknownWhenOriginalPriceIsMissing() {
        SuggestPriceRequest request = new SuggestPriceRequest();
        request.setConditionLevel("全新");

        SuggestPriceResponse response = aiService.suggestPrice(request);

        assertEquals("UNKNOWN", response.getPriceStatus());
        assertEquals("缺少商品原价，暂时无法生成准确的价格建议。", response.getWarning());
        assertEquals(null, response.getMinPrice());
    }

    @Test
    void shouldReturnUnknownWhenCurrentPriceIsMissing() {
        SuggestPriceRequest request = new SuggestPriceRequest();
        request.setOriginalPrice(new BigDecimal("200"));
        request.setConditionLevel("七成新");

        SuggestPriceResponse response = aiService.suggestPrice(request);

        assertEquals(new BigDecimal("70.00"), response.getMinPrice());
        assertEquals(new BigDecimal("100.00"), response.getMaxPrice());
        assertEquals("UNKNOWN", response.getPriceStatus());
        assertEquals("缺少必要信息，暂时无法判断价格是否合理。", response.getWarning());
    }

    @Test
    void shouldClampRatesAndDetectLowPrice() {
        SuggestPriceRequest request = new SuggestPriceRequest();
        request.setCategory("生活用品");
        request.setOriginalPrice(new BigDecimal("100"));
        request.setConditionLevel("五成新");
        request.setUsedTime("三年");
        request.setPrice(new BigDecimal("4"));

        SuggestPriceResponse response = aiService.suggestPrice(request);

        assertEquals(new BigDecimal("5.00"), response.getMinPrice());
        assertEquals(new BigDecimal("5.00"), response.getMaxPrice());
        assertEquals("TOO_LOW", response.getPriceStatus());
    }

    @Test
    void shouldUseDeepSeekDescriptionWhenAvailable() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.isConfigured()).thenReturn(true);
        when(client.chat(anyString(), anyString(), anyBoolean()))
                .thenReturn("九成新的 iPad Air 5，功能正常，支持校内线下面交。");
        AiServiceImpl service = new AiServiceImpl(
                client, new AiPromptBuilder(), new ObjectMapper());

        GenerateDescriptionRequest request = new GenerateDescriptionRequest();
        request.setName("iPad Air 5");

        assertEquals(
                "九成新的 iPad Air 5，功能正常，支持校内线下面交。",
                service.generateDescription(request).getDescription());
    }

    @Test
    void shouldFallbackWhenDeepSeekDescriptionFails() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.isConfigured()).thenReturn(true);
        when(client.chat(anyString(), anyString(), anyBoolean()))
                .thenThrow(new IllegalStateException("timeout"));
        AiServiceImpl service = new AiServiceImpl(
                client, new AiPromptBuilder(), new ObjectMapper());

        GenerateDescriptionRequest request = new GenerateDescriptionRequest();
        request.setName("高等数学教材");
        request.setCategory("教材书籍");

        assertTrue(service.generateDescription(request).getDescription()
                .contains("适合课程学习、复习备考和资料查阅"));
    }

    @Test
    void shouldParseDeepSeekTradeAdviceJson() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.isConfigured()).thenReturn(true);
        when(client.chat(anyString(), anyString(), anyBoolean())).thenReturn(
                "{\"advice\":\"建议当面验货\","
                        + "\"checkItems\":[\"检查屏幕\"],"
                        + "\"questions\":[\"是否维修过\"],"
                        + "\"safetyTips\":[\"校内面交\"],"
                        + "\"riskLevel\":\"LOW\"}");
        AiServiceImpl service = new AiServiceImpl(
                client, new AiPromptBuilder(), new ObjectMapper());

        TradeAdviceResponse response = service.tradeAdvice(new TradeAdviceRequest());

        assertEquals("建议当面验货", response.getAdvice());
        assertEquals("LOW", response.getRiskLevel());
        assertEquals("检查屏幕", response.getCheckItems().get(0));
    }

    @Test
    void shouldFallbackForInvalidTradeAdviceJsonAndDetectRisk() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.isConfigured()).thenReturn(true);
        when(client.chat(anyString(), anyString(), anyBoolean()))
                .thenReturn("这不是合法 JSON");
        AiServiceImpl service = new AiServiceImpl(
                client, new AiPromptBuilder(), new ObjectMapper());

        TradeAdviceRequest request = new TradeAdviceRequest();
        request.setProductName("iPad Air 5");
        request.setCategory("数码产品");
        request.setDescription("支持微信转账后发货");
        request.setPrice(new BigDecimal("2800"));
        request.setOriginalPrice(new BigDecimal("4500"));

        TradeAdviceResponse response = service.tradeAdvice(request);

        assertEquals("HIGH", response.getRiskLevel());
        assertTrue(response.getAdvice().contains("提前付款"));
        assertTrue(response.getCheckItems().contains("查看电池健康度、序列号和维修记录"));
        assertFalse(response.getSafetyTips().isEmpty());
    }

    @Test
    void shouldMarkVeryLowPriceAsMediumRiskWithoutKeywords() {
        TradeAdviceRequest request = new TradeAdviceRequest();
        request.setCategory("教材书籍");
        request.setPrice(new BigDecimal("20"));
        request.setOriginalPrice(new BigDecimal("100"));

        TradeAdviceResponse response = aiService.tradeAdvice(request);

        assertEquals("MEDIUM", response.getRiskLevel());
        assertTrue(response.getCheckItems().contains("核对教材版本、出版社和课程要求"));
    }
}
