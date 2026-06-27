package com.sparkflow.dto;

import java.util.List;

public record NodeDto(
        String id,
        String nodeType,
        String title,
        String summary,
        String detail,
        Double posX,
        Double posY
) {}
