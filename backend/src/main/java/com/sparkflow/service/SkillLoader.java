package com.sparkflow.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SkillLoader {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private static final Map<String, String> SKILLS = Map.of(
            "warm-spark", "skills/warm-spark.md",
            "minimal-motion", "skills/minimal-motion.md",
            "tech-blueprint", "skills/tech-blueprint.md",
            "remotion-storyboard", "skills/remotion-storyboard.md"
    );

    public String loadSkill(String style) {
        String key = style != null && !style.isBlank() ? style : "warm-spark";
        return CACHE.computeIfAbsent(key, this::readSkill);
    }

    public Map<String, String> listSkills() {
        return SKILLS;
    }

    private String readSkill(String key) {
        String path = SKILLS.getOrDefault(key, SKILLS.get("warm-spark"));
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "使用暖白燕麦色调，圆角卡片，CSS 动画，单页 HTML，无需外部 CDN。";
        }
    }
}
