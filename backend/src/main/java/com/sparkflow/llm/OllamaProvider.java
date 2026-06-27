package com.sparkflow.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparkflow.dto.MindmapResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

public class OllamaProvider extends AbstractLLMProvider {

    public OllamaProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                          String apiKey, String baseUrl) {
        super(webClientBuilder, objectMapper, apiKey,
                baseUrl != null && !baseUrl.isBlank() ? baseUrl : "http://localhost:11434");
    }

    @Override
    public MindmapResponse generateMindmap(String prompt, String modelName) {
        try {
            return generateMindmapInternal(prompt, modelName);
        } catch (Exception e) {
            return createFallbackMindmap("Ollama 响应解析失败");
        }
    }

    @Override
    protected String generateText(String systemPrompt, String userPrompt, String modelName, boolean jsonMode) {
        Map<String, Object> body = jsonMode
                ? Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "stream", false,
                "format", "json"
        )
                : Map.of(
                "model", modelName,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "stream", false
        );

        String response = callChatApi(baseUrl + "/api/chat", body, null);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Ollama 响应解析失败", e);
        }
    }
}
