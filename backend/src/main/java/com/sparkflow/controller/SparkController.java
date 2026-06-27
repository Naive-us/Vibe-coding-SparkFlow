package com.sparkflow.controller;

import com.sparkflow.dto.SessionDetailDto;
import com.sparkflow.dto.SessionSummaryDto;
import com.sparkflow.dto.SparkRequest;
import com.sparkflow.service.SparkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SparkController {

    private final SparkService sparkService;

    public SparkController(SparkService sparkService) {
        this.sparkService = sparkService;
    }

    @PostMapping("/spark")
    public SessionDetailDto spark(@Valid @RequestBody SparkRequest request) {
        try {
            return sparkService.createSpark(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/sessions")
    public List<SessionSummaryDto> listSessions() {
        return sparkService.listSessions();
    }

    @GetMapping("/sessions/{id}")
    public SessionDetailDto getSession(@PathVariable String id) {
        try {
            return sparkService.getSession(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/sessions/{id}/inspirations/{index}")
    public SessionDetailDto deleteInspiration(@PathVariable String id, @PathVariable int index) {
        try {
            return sparkService.deleteInspiration(id, index);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/sessions/{id}")
    public Map<String, String> deleteSession(@PathVariable String id) {
        try {
            sparkService.deleteSession(id);
            return Map.of("status", "ok");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/skills")
    public List<String> listSkills() {
        return sparkService.listSkills();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "app", "SparkFlow");
    }
}
