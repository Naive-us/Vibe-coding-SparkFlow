package com.sparkflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SessionDetailDto(
        String id,
        String title,
        String rawInput,
        String provider,
        String modelName,
        LocalDateTime createdAt,
        String parentSessionId,
        String conversationLog,
        String demoHtml,
        List<NodeDto> nodes,
        List<EdgeDto> edges
) {}
