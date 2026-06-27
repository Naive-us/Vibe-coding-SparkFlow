package com.sparkflow.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparkflow.dto.MindmapResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractLLMProvider implements BaseLLMProvider {

    protected final WebClient webClient;
    protected final ObjectMapper objectMapper;
    protected final String apiKey;
    protected final String baseUrl;

    protected AbstractLLMProvider(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                                  String apiKey, String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    protected static final String SYSTEM_PROMPT = """
            你是 SparkFlow 的思维导图架构师。用户会用自然语言描述一个想法，你要把它重构成清晰、有层级、有行动感的思维导图。

            生成原则：
            - 只返回严格 JSON，不要 markdown 代码块，不要解释。
            - 生成 5-9 个节点：1 个中心节点，3-5 个一级分支，必要时 1-3 个二级补充节点。
            - 中心节点必须是 custom_anchor，表达这个想法的核心命题。
            - custom_context 用来表示背景、问题、对象、场景。
            - custom_value 用来表示价值、方案、行动、判断、机会。
            - 每个节点 title 2-8 个汉字，summary 2-6 个汉字，detail 18-45 个汉字。
            - 不要把用户原话切碎成词云；要提炼出“为什么、给谁、怎么做、会产生什么价值、下一步是什么”。
            - 边必须形成清晰层级：中心节点连接一级分支，二级节点连接所属一级分支。不要生成孤立节点。
            - 坐标使用 800x600 画布：中心 posX=400,posY=300；一级分支围绕中心分布；二级节点靠近父节点外侧。

            JSON 格式如下：
            {
              "title": "简短标题",
              "nodes": [
                {
                  "id": "node-1",
                  "nodeType": "custom_anchor",
                  "title": "节点标题",
                  "summary": "核心",
                  "detail": "详细说明",
                  "posX": 400,
                  "posY": 300
                }
              ],
              "edges": [
                { "sourceId": "node-1", "targetId": "node-2" }
              ]
            }
            """;

    protected static final String DEMO_SYSTEM_PROMPT = """
            你是一个可视化灵感动画生成器。输出完整单页 HTML，内联 CSS + JS，无 CDN。
            使用 SVG 或 HTML 图形做 3-6 步演示，每步短标签、清晰连线、逐步显现，适配 iframe。
            """;

    @Override
    public String generateDemoHtml(String prompt, String modelName) {
        return generateText(DEMO_SYSTEM_PROMPT, prompt, modelName, false);
    }

    protected abstract String generateText(String systemPrompt, String userPrompt, String modelName, boolean jsonMode);

    protected MindmapResponse generateMindmapInternal(String prompt, String modelName) {
        String content = generateText(SYSTEM_PROMPT, prompt, modelName, true);
        return normalizeMindmap(parseResponse(content));
    }

    protected MindmapResponse parseResponse(String content) {
        try {
            String json = extractJson(content);
            JsonNode root = objectMapper.readTree(json);

            String title = root.path("title").asText("未命名灵感");
            List<MindmapResponse.MindmapNode> nodes = new ArrayList<>();
            List<MindmapResponse.MindmapEdge> edges = new ArrayList<>();

            JsonNode nodesArray = root.path("nodes");
            if (nodesArray.isArray()) {
                int index = 0;
                for (JsonNode n : nodesArray) {
                    String id = n.path("id").asText("node-" + UUID.randomUUID());
                    String nodeType = n.path("nodeType").asText("custom_context");
                    String nodeTitle = n.path("title").asText("节点");
                    String summary = n.path("summary").asText("");
                    String detail = n.path("detail").asText("");
                    double posX = n.path("posX").asDouble(200 + index * 150);
                    double posY = n.path("posY").asDouble(200 + (index % 3) * 100);
                    nodes.add(new MindmapResponse.MindmapNode(id, nodeType, nodeTitle, summary, detail, posX, posY));
                    index++;
                }
            }

            JsonNode edgesArray = root.path("edges");
            if (edgesArray.isArray()) {
                for (JsonNode e : edgesArray) {
                    edges.add(new MindmapResponse.MindmapEdge(
                            e.path("sourceId").asText(),
                            e.path("targetId").asText()
                    ));
                }
            }

            if (nodes.isEmpty()) {
                return createFallbackMindmap(title);
            }
            return new MindmapResponse(title, nodes, edges);
        } catch (Exception ex) {
            return createFallbackMindmap("解析失败");
        }
    }

    private MindmapResponse normalizeMindmap(MindmapResponse mindmap) {
        if (mindmap == null || mindmap.nodes() == null || mindmap.nodes().isEmpty()) {
            return createFallbackMindmap("SparkFlow");
        }

        List<MindmapResponse.MindmapNode> sourceNodes = mindmap.nodes();
        int centerIndex = findCenterIndex(sourceNodes);
        MindmapResponse.MindmapNode center = sourceNodes.get(centerIndex);

        List<MindmapResponse.MindmapNode> nodes = new ArrayList<>();
        nodes.add(new MindmapResponse.MindmapNode(
                center.id(),
                "custom_anchor",
                center.title(),
                center.summary(),
                center.detail(),
                400.0,
                300.0
        ));

        List<MindmapResponse.MindmapNode> branches = new ArrayList<>();
        for (int i = 0; i < sourceNodes.size(); i++) {
            if (i != centerIndex) {
                branches.add(sourceNodes.get(i));
            }
        }

        int limit = Math.min(branches.size(), 8);
        for (int i = 0; i < limit; i++) {
            MindmapResponse.MindmapNode node = branches.get(i);
            double[] pos = branchPosition(i, limit);
            nodes.add(new MindmapResponse.MindmapNode(
                    node.id(),
                    normalizeNodeType(node.nodeType(), i),
                    node.title(),
                    node.summary(),
                    node.detail(),
                    pos[0],
                    pos[1]
            ));
        }

        return new MindmapResponse(mindmap.title(), nodes, normalizeEdges(mindmap.edges(), nodes));
    }

    private int findCenterIndex(List<MindmapResponse.MindmapNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            if ("custom_anchor".equals(nodes.get(i).nodeType())) {
                return i;
            }
        }
        return 0;
    }

    private String normalizeNodeType(String nodeType, int index) {
        if ("custom_context".equals(nodeType) || "custom_value".equals(nodeType)) {
            return nodeType;
        }
        return index % 2 == 0 ? "custom_context" : "custom_value";
    }

    private double[] branchPosition(int index, int total) {
        double[][] anchors = {
                {400, 105}, {650, 205}, {625, 425}, {400, 500},
                {175, 425}, {150, 205}, {585, 115}, {215, 115}
        };
        if (total <= anchors.length) {
            return anchors[index];
        }
        double angle = -Math.PI / 2 + 2 * Math.PI * index / Math.max(total, 1);
        return new double[]{400 + Math.cos(angle) * 250, 300 + Math.sin(angle) * 200};
    }

    private List<MindmapResponse.MindmapEdge> normalizeEdges(List<MindmapResponse.MindmapEdge> rawEdges,
                                                            List<MindmapResponse.MindmapNode> nodes) {
        Set<String> nodeIds = new LinkedHashSet<>();
        for (MindmapResponse.MindmapNode node : nodes) {
            nodeIds.add(node.id());
        }

        List<MindmapResponse.MindmapEdge> edges = new ArrayList<>();
        Set<String> connected = new HashSet<>();
        if (rawEdges != null) {
            for (MindmapResponse.MindmapEdge edge : rawEdges) {
                if (nodeIds.contains(edge.sourceId()) && nodeIds.contains(edge.targetId())
                        && !edge.sourceId().equals(edge.targetId())) {
                    edges.add(edge);
                    connected.add(edge.sourceId());
                    connected.add(edge.targetId());
                }
            }
        }

        String centerId = nodes.get(0).id();
        for (int i = 1; i < nodes.size(); i++) {
            String id = nodes.get(i).id();
            if (!connected.contains(id)) {
                edges.add(new MindmapResponse.MindmapEdge(centerId, id));
            }
        }
        return edges;
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }

    protected MindmapResponse createFallbackMindmap(String title) {
        String safeTitle = title != null && !title.isBlank() ? title : "灵感";
        return new MindmapResponse(
                safeTitle,
                List.of(
                        new MindmapResponse.MindmapNode("center", "custom_anchor", safeTitle, "核心", "围绕当前想法提炼出的核心命题。", 400.0, 300.0),
                        new MindmapResponse.MindmapNode("branch-1", "custom_context", "问题场景", "场景", "先明确这个灵感要回应的真实处境。", 400.0, 105.0),
                        new MindmapResponse.MindmapNode("branch-2", "custom_value", "关键价值", "价值", "提炼它能带来的改变和判断标准。", 650.0, 205.0),
                        new MindmapResponse.MindmapNode("branch-3", "custom_value", "下一步", "行动", "把灵感落到一个可以继续推进的动作。", 625.0, 425.0)
                ),
                List.of(
                        new MindmapResponse.MindmapEdge("center", "branch-1"),
                        new MindmapResponse.MindmapEdge("center", "branch-2"),
                        new MindmapResponse.MindmapEdge("center", "branch-3")
                )
        );
    }

    protected String callChatApi(String url, Map<String, Object> body, String authHeader) {
        WebClient.RequestHeadersSpec<?> spec = webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .bodyValue(body);

        if (authHeader != null) {
            spec = spec.header("Authorization", authHeader);
        }

        return spec.retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
