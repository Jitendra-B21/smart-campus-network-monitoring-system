package com.network.management.nms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("system", "Smart Campus NMS");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("monitoring", "ACTIVE");
        response.put("alarmEngine", "ACTIVE");
        response.put("observability", "ACTIVE");

        return response;
    }
}