package com.sparkflow.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparkflow.dto.MindmapResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAICompatibleProvider extends AbstractLLMProvider {

    private final String label;

    public OpenAICompatibleProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                                    String apiKey, String baseUrl, String label, String defaultBaseUrl) {
        super(webClientBuilder, objectMapper, apiKey,
                baseUrl != null && !baseUrl.isBlank() ? baseUrl : defaultBaseUrl);
        this.label = label;
    }

    @Override
    public MindmapResponse generateMindmap(String prompt, String modelName) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(label + " API Key 未配置");
        }
        return generateMindmapInternal(prompt, modelName);
    }

    @Override
    protected String generateText(String systemPrompt, String userPrompt, String modelName, boolean jsonMode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(label + " API Key 未配置");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", 0.7);
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        String response = callChatApi(baseUrl + "/chat/completions", body, "Bearer " + apiKey);
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException(label + " 响应解析失败", e);
        }
    }
}
