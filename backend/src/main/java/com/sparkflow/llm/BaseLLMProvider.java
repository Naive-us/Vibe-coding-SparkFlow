package com.sparkflow.llm;

import com.sparkflow.dto.MindmapResponse;

public interface BaseLLMProvider {
    MindmapResponse generateMindmap(String prompt, String modelName);
    String generateDemoHtml(String prompt, String modelName);
}
