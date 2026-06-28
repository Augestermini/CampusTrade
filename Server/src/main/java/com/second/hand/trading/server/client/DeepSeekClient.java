package com.second.hand.trading.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.config.DeepSeekProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public DeepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout());
        requestFactory.setReadTimeout(properties.getTimeout());
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String chat(String systemPrompt, String userPrompt, boolean jsonMode) {
        if (!isConfigured()) {
            throw new IllegalStateException("DeepSeek API key is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey().trim());

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("messages", buildMessages(systemPrompt, userPrompt));
        requestBody.put("temperature", 0.4);
        requestBody.put("stream", false);
        requestBody.put("thinking", thinkingDisabled());
        if (jsonMode) {
            requestBody.put("response_format", jsonResponseFormat());
        }

        ResponseEntity<String> response = restTemplate.postForEntity(
                buildChatUrl(),
                new HttpEntity<>(requestBody, headers),
                String.class);

        return extractContent(response.getBody());
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, String userPrompt) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt));
        return messages;
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content == null ? "" : content);
        return message;
    }

    private Map<String, String> thinkingDisabled() {
        Map<String, String> thinking = new LinkedHashMap<>();
        thinking.put("type", "disabled");
        return thinking;
    }

    private Map<String, String> jsonResponseFormat() {
        Map<String, String> responseFormat = new LinkedHashMap<>();
        responseFormat.put("type", "json_object");
        return responseFormat;
    }

    private String extractContent(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            return content.isTextual() ? content.asText().trim() : "";
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse DeepSeek response", exception);
        }
    }

    private String buildChatUrl() {
        String baseUrl = properties.getBaseUrl() == null
                ? "https://api.deepseek.com"
                : properties.getBaseUrl().trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + CHAT_COMPLETIONS_PATH;
    }
}
