package com.sparkflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConversationLogHelper {

    private final ObjectMapper objectMapper;

    public ConversationLogHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String append(String existingLog, String role, String content) {
        List<Map<String, String>> entries = parse(existingLog);
        entries.add(entry(role, content));
        return serialize(entries);
    }

    public String initFromPrevious(String previousLog, String newUserInput) {
        return append(previousLog, "user", newUserInput);
    }

    public String removeUserEntry(String log, int userIndex) {
        List<Map<String, String>> entries = parse(log);
        int userCount = 0;
        int removeAt = -1;

        for (int i = 0; i < entries.size(); i++) {
            if ("user".equals(entries.get(i).get("role"))) {
                if (userCount == userIndex) {
                    removeAt = i;
                    break;
                }
                userCount++;
            }
        }

        if (removeAt < 0) {
            throw new IllegalArgumentException("灵感条目不存在");
        }

        entries.remove(removeAt);
        if (removeAt < entries.size() && "assistant".equals(entries.get(removeAt).get("role"))) {
            entries.remove(removeAt);
        }

        return serialize(entries);
    }

    public String rebuildRawInput(String log) {
        return parse(log).stream()
                .filter(e -> "user".equals(e.get("role")))
                .map(e -> e.get("content"))
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.joining("\n\n· "));
    }

    public int countUserEntries(String log) {
        return (int) parse(log).stream().filter(e -> "user".equals(e.get("role"))).count();
    }

    private Map<String, String> entry(String role, String content) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("role", role);
        map.put("content", content);
        map.put("timestamp", LocalDateTime.now().toString());
        return map;
    }

    private List<Map<String, String>> parse(String log) {
        if (log == null || log.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(objectMapper.readValue(log, new TypeReference<>() {}));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serialize(List<Map<String, String>> entries) {
        try {
            return objectMapper.writeValueAsString(entries);
        } catch (Exception e) {
            return "[]";
        }
    }
}
