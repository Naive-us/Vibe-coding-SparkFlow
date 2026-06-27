package com.sparkflow.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

public class OpenAIProvider extends OpenAICompatibleProvider {

    public OpenAIProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                          String apiKey, String baseUrl) {
        super(webClientBuilder, objectMapper, apiKey, baseUrl, "OpenAI", "https://api.openai.com/v1");
    }
}
