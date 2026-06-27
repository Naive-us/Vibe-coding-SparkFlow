package com.sparkflow.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

public class DeepSeekProvider extends OpenAICompatibleProvider {

    public DeepSeekProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                            String apiKey, String baseUrl) {
        super(webClientBuilder, objectMapper, apiKey, baseUrl, "DeepSeek", "https://api.deepseek.com/v1");
    }
}
