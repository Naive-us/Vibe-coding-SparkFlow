package com.sparkflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawInput;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 36)
    private String parentSessionId;

    @Column(columnDefinition = "TEXT")
    private String conversationLog;

    @Column(columnDefinition = "TEXT")
    private String demoHtml;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Node> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Edge> edges = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getParentSessionId() { return parentSessionId; }
    public void setParentSessionId(String parentSessionId) { this.parentSessionId = parentSessionId; }
    public String getConversationLog() { return conversationLog; }
    public void setConversationLog(String conversationLog) { this.conversationLog = conversationLog; }
    public String getDemoHtml() { return demoHtml; }
    public void setDemoHtml(String demoHtml) { this.demoHtml = demoHtml; }
    public List<Node> getNodes() { return nodes; }
    public void setNodes(List<Node> nodes) { this.nodes = nodes; }
    public List<Edge> getEdges() { return edges; }
    public void setEdges(List<Edge> edges) { this.edges = edges; }
}
