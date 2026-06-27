package com.sparkflow.service;

import com.sparkflow.dto.MindmapResponse;
import com.sparkflow.entity.Session;
import org.springframework.stereotype.Service;

@Service
public class DemoGenerationService {

    private final DemoHtmlBuilder demoHtmlBuilder;

    public DemoGenerationService(DemoHtmlBuilder demoHtmlBuilder) {
        this.demoHtmlBuilder = demoHtmlBuilder;
    }

    /** 由思维导图直接生成纯图形 SVG 动画，无 LLM 文字输出。 */
    public String generateFromMindmap(MindmapResponse mindmap, String skillStyle) {
        return demoHtmlBuilder.build(mindmap, skillStyle);
    }

    public static String buildContext(Session session, String newInput) {
        if (session == null) {
            return newInput;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("会话标题: ").append(session.getTitle()).append("\n");
        sb.append("已累计灵感:\n").append(session.getRawInput()).append("\n");
        if (session.getConversationLog() != null) {
            sb.append("对话记录: ").append(session.getConversationLog()).append("\n");
        }
        if (!session.getNodes().isEmpty()) {
            sb.append("当前图谱节点:\n");
            session.getNodes().forEach(n ->
                    sb.append("- [").append(n.getSummary()).append("] ").append(n.getTitle()).append("\n"));
        }
        sb.append("\n本轮追加灵感: ").append(newInput);
        sb.append("\n\n请在已有图谱基础上扩展，融合全部累计灵感，保留核心节点。");
        return sb.toString();
    }
}
