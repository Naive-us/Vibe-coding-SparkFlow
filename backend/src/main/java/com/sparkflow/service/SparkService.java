package com.sparkflow.service;

import com.sparkflow.dto.*;
import com.sparkflow.entity.Edge;
import com.sparkflow.entity.Node;
import com.sparkflow.entity.Session;
import com.sparkflow.llm.BaseLLMProvider;
import com.sparkflow.llm.LLMFactory;
import com.sparkflow.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SparkService {

    private final SessionRepository sessionRepository;
    private final LLMFactory llmFactory;
    private final DemoGenerationService demoGenerationService;
    private final ConversationLogHelper conversationLogHelper;
    private final SkillLoader skillLoader;
    private final McpClient mcpClient;

    public SparkService(SessionRepository sessionRepository, LLMFactory llmFactory,
                        DemoGenerationService demoGenerationService,
                        ConversationLogHelper conversationLogHelper,
                        SkillLoader skillLoader,
                        McpClient mcpClient) {
        this.sessionRepository = sessionRepository;
        this.llmFactory = llmFactory;
        this.demoGenerationService = demoGenerationService;
        this.conversationLogHelper = conversationLogHelper;
        this.skillLoader = skillLoader;
        this.mcpClient = mcpClient;
    }

    @Transactional
    public SessionDetailDto createSpark(SparkRequest request) {
        BaseLLMProvider provider = llmFactory.getProvider(
                request.provider(), request.apiKey(), request.baseUrl());

        Session session;
        String contextPrompt;

        if (request.isContinuingSession()) {
            session = sessionRepository.findById(request.sessionId())
                    .orElseThrow(() -> new IllegalArgumentException("当前会话不存在，请开启新灵感"));
            contextPrompt = DemoGenerationService.buildContext(session, request.rawInput());
            accumulateRawInput(session, request.rawInput());
            session.setConversationLog(conversationLogHelper.append(
                    session.getConversationLog(), "user", request.rawInput()));
            session.setProvider(request.provider());
            session.setModelName(request.modelName());
            session.getNodes().clear();
            session.getEdges().clear();
        } else {
            session = new Session();
            session.setRawInput(request.rawInput());
            session.setProvider(request.provider());
            session.setModelName(request.modelName());
            session.setConversationLog(conversationLogHelper.append(null, "user", request.rawInput()));
            contextPrompt = request.rawInput();
        }

        MindmapResponse mindmap = provider.generateMindmap(contextPrompt, request.modelName());
        session.setTitle(mindmap.title());
        session.setConversationLog(conversationLogHelper.append(
                session.getConversationLog(), "assistant", "已更新思维导图：" + mindmap.title()));

        applyMindmap(session, mindmap);

        if (request.shouldGenerateDemo()) {
            String mcpHtml = mcpClient.generateHtml(
                    request.mcpUrl(),
                    request.rawInput(),
                    request.skillStyle(),
                    contextPrompt
            );
            session.setDemoHtml(mcpHtml != null
                    ? mcpHtml
                    : demoGenerationService.generateFromMindmap(mindmap, request.skillStyle()));
        }

        Session saved = sessionRepository.save(session);
        return toDetailDto(saved);
    }

    private void accumulateRawInput(Session session, String newInput) {
        String existing = session.getRawInput();
        if (existing == null || existing.isBlank()) {
            session.setRawInput(newInput);
        } else {
            session.setRawInput(existing + "\n\n· " + newInput);
        }
    }

    private void applyMindmap(Session session, MindmapResponse mindmap) {
        Map<String, String> idMapping = new HashMap<>();
        Set<String> nodeIds = new HashSet<>();

        for (MindmapResponse.MindmapNode mn : mindmap.nodes()) {
            String newId = UUID.randomUUID().toString();
            if (mn.id() != null && !mn.id().isBlank()) {
                idMapping.put(mn.id(), newId);
            }

            Node node = new Node();
            node.setId(newId);
            node.setSession(session);
            node.setNodeType(mn.nodeType());
            node.setTitle(mn.title());
            node.setSummary(mn.summary());
            node.setDetail(mn.detail());
            node.setPosX(mn.posX());
            node.setPosY(mn.posY());
            session.getNodes().add(node);
            nodeIds.add(newId);
        }

        for (MindmapResponse.MindmapEdge me : mindmap.edges()) {
            String sourceId = idMapping.getOrDefault(me.sourceId(), me.sourceId());
            String targetId = idMapping.getOrDefault(me.targetId(), me.targetId());
            if (!nodeIds.contains(sourceId) || !nodeIds.contains(targetId)) {
                continue;
            }
            Edge edge = new Edge();
            edge.setSession(session);
            edge.setSourceId(sourceId);
            edge.setTargetId(targetId);
            session.getEdges().add(edge);
        }
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryDto> listSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(s -> new SessionSummaryDto(
                        s.getId(), s.getTitle(), s.getProvider(), s.getModelName(), s.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailDto getSession(String id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + id));
        return toDetailDto(session);
    }

    @Transactional(readOnly = true)
    public List<String> listSkills() {
        return List.copyOf(skillLoader.listSkills().keySet());
    }

    @Transactional
    public SessionDetailDto deleteInspiration(String sessionId, int userIndex) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + sessionId));

        if (conversationLogHelper.countUserEntries(session.getConversationLog()) == 0) {
            if (userIndex == 0 && session.getRawInput() != null && !session.getRawInput().isBlank()) {
                session.setRawInput("");
                session.setConversationLog("[]");
                return toDetailDto(sessionRepository.save(session));
            }
            throw new IllegalArgumentException("灵感条目不存在");
        }

        String updatedLog = conversationLogHelper.removeUserEntry(session.getConversationLog(), userIndex);
        session.setConversationLog(updatedLog);
        session.setRawInput(conversationLogHelper.rebuildRawInput(updatedLog));

        return toDetailDto(sessionRepository.save(session));
    }

    @Transactional
    public void deleteSession(String sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        sessionRepository.deleteById(sessionId);
    }

    private SessionDetailDto toDetailDto(Session session) {
        List<NodeDto> nodes = session.getNodes().stream()
                .map(n -> new NodeDto(n.getId(), n.getNodeType(), n.getTitle(),
                        n.getSummary(), n.getDetail(), n.getPosX(), n.getPosY()))
                .toList();
        List<EdgeDto> edges = session.getEdges().stream()
                .map(e -> new EdgeDto(e.getId(), e.getSourceId(), e.getTargetId()))
                .toList();
        return new SessionDetailDto(
                session.getId(), session.getTitle(), session.getRawInput(),
                session.getProvider(), session.getModelName(), session.getCreatedAt(),
                session.getParentSessionId(), session.getConversationLog(), session.getDemoHtml(),
                nodes, edges);
    }
}
