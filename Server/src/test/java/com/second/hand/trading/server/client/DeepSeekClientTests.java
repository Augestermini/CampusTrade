package com.second.hand.trading.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.config.DeepSeekProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepSeekClientTests {

    @Test
    void shouldSendOpenAiCompatibleRequestAndParseContent() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(readBody(exchange));
            writeJson(exchange,
                    "{\"choices\":[{\"message\":{\"content\":\"{\\\"riskLevel\\\":\\\"LOW\\\"}\"}}]}");
        });
        server.start();

        try {
            DeepSeekProperties properties = new DeepSeekProperties();
            properties.setApiKey("test-api-key");
            properties.setBaseUrl("http://localhost:" + server.getAddress().getPort());
            properties.setModel("deepseek-v4-pro");
            properties.setTimeout(3000);
            DeepSeekClient client = new DeepSeekClient(properties, objectMapper);

            String content = client.chat("系统提示", "用户提示", true);

            assertEquals("{\"riskLevel\":\"LOW\"}", content);
            assertEquals("Bearer test-api-key", authorization.get());
            JsonNode request = objectMapper.readTree(requestBody.get());
            assertEquals("deepseek-v4-pro", request.path("model").asText());
            assertEquals(false, request.path("stream").asBoolean());
            assertEquals("disabled", request.path("thinking").path("type").asText());
            assertEquals("json_object", request.path("response_format").path("type").asText());
            assertEquals("系统提示", request.path("messages").path(0).path("content").asText());
            assertTrue(request.path("temperature").asDouble() == 0.4);
        } finally {
            server.stop(0);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private void writeJson(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
