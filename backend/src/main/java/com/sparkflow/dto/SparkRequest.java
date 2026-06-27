package com.sparkflow.dto;

import jakarta.validation.constraints.NotBlank;

public record SparkRequest(
        @NotBlank(message = "输入内容不能为空")
        String rawInput,
        @NotBlank(message = "Provider 不能为空")
        String provider,
        @NotBlank(message = "Model 不能为空")
        String modelName,
        String apiKey,
        String baseUrl,
        String sessionId,
        Boolean generateDemo,
        String skillStyle,
        String mcpUrl
) {
    public boolean shouldGenerateDemo() {
        return Boolean.TRUE.equals(generateDemo);
    }

    public boolean isContinuingSession() {
        return sessionId != null && !sessionId.isBlank();
    }
}
