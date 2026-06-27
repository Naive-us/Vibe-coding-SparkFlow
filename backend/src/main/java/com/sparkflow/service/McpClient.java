package com.sparkflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class McpClient {

    private static final Logger log = LoggerFactory.getLogger(McpClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public McpClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateHtml(String mcpUrl, String prompt, String skillStyle, String context) {
        if (mcpUrl == null || mcpUrl.isBlank()) {
            return null;
        }
        try {
            Map<String, String> body = Map.of(
                    "prompt", prompt,
                    "style", skillStyle != null ? skillStyle : "warm-spark",
                    "context", context != null ? context : ""
            );
            String response = webClient.post()
                    .uri(mcpUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(90))
                    .block();

            if (response == null || response.isBlank()) {
                return null;
            }

            if (response.trim().startsWith("<")) {
                return response;
            }

            JsonNode root = objectMapper.readTree(response);
            String html = root.path("html").asText(null);
            if (html != null && !html.isBlank()) {
                return html;
            }
            return root.path("content").asText(null);
        } catch (Exception e) {
            log.warn("MCP 调用失败，将回退到本地 LLM 生成: {}", e.getMessage());
            return null;
        }
    }
}
