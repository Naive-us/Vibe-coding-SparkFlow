package com.sparkflow.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;

public class QwenProvider extends OpenAICompatibleProvider {

    public QwenProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                        String apiKey, String baseUrl) {
        super(webClientBuilder, objectMapper, apiKey, baseUrl, "通义千问",
                "https://dashscope.aliyuncs.com/compatible-mode/v1");
    }
}
