package com.sparkflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SessionSummaryDto(
        String id,
        String title,
        String provider,
        String modelName,
        LocalDateTime createdAt
) {}
