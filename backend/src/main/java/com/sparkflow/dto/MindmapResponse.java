package com.sparkflow.dto;

import java.util.List;

public record MindmapResponse(
        String title,
        List<MindmapNode> nodes,
        List<MindmapEdge> edges
) {
    public record MindmapNode(
            String id,
            String nodeType,
            String title,
            String summary,
            String detail,
            Double posX,
            Double posY
    ) {}

    public record MindmapEdge(
            String sourceId,
            String targetId
    ) {}
}
