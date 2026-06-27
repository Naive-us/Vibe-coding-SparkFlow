package com.sparkflow.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparkflow.dto.MindmapResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

public class AnthropicProvider extends AbstractLLMProvider {

    public AnthropicProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                             String apiKey, String baseUrl) {
        super(webClientBuilder, objectMapper, apiKey,
                baseUrl != null && !baseUrl.isBlank() ? baseUrl : "https://api.anthropic.com/v1");
    }

    @Override
    public MindmapResponse generateMindmap(String prompt, String modelName) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Anthropic API Key 未配置");
        }
        try {
            return generateMindmapInternal(prompt, modelName);
        } catch (Exception e) {
            return createFallbackMindmap("Anthropic 响应解析失败");
        }
    }

    @Override
    protected String generateText(String systemPrompt, String userPrompt, String modelName, boolean jsonMode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Anthropic API Key 未配置");
        }

        String userContent = jsonMode
                ? userPrompt + "\n\n请严格返回 JSON，不要 markdown 代码块。"
                : userPrompt;

        Map<String, Object> body = Map.of(
                "model", modelName,
                "max_tokens", 8192,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userContent)
                )
        );

        String response = webClient.post()
                .uri(baseUrl + "/messages")
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("content").path(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("Anthropic 响应解析失败", e);
        }
    }
}
