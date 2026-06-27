package com.sparkflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private Session session;

    @Column(nullable = false)
    private String nodeType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false)
    private Double posX;

    @Column(nullable = false)
    private Double posY;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }
    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }
}
