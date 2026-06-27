package com.sparkflow.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class LLMFactory {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final Map<String, Function<ProviderConfig, BaseLLMProvider>> providers = new HashMap<>();

    public LLMFactory(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        registerProvider("openai", cfg -> new OpenAIProvider(webClientBuilder, objectMapper, cfg.apiKey(), cfg.baseUrl()));
        registerProvider("anthropic", cfg -> new AnthropicProvider(webClientBuilder, objectMapper, cfg.apiKey(), cfg.baseUrl()));
        registerProvider("ollama", cfg -> new OllamaProvider(webClientBuilder, objectMapper, cfg.apiKey(), cfg.baseUrl()));
        registerProvider("qwen", cfg -> new QwenProvider(webClientBuilder, objectMapper, cfg.apiKey(), cfg.baseUrl()));
        registerProvider("deepseek", cfg -> new DeepSeekProvider(webClientBuilder, objectMapper, cfg.apiKey(), cfg.baseUrl()));
    }

    public void registerProvider(String name, Function<ProviderConfig, BaseLLMProvider> creator) {
        providers.put(name.toLowerCase(), creator);
    }

    public BaseLLMProvider getProvider(String name, String apiKey, String baseUrl) {
        Function<ProviderConfig, BaseLLMProvider> creator = providers.get(name.toLowerCase());
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported provider: " + name);
        }
        return creator.apply(new ProviderConfig(apiKey, baseUrl));
    }

    public record ProviderConfig(String apiKey, String baseUrl) {}
}
